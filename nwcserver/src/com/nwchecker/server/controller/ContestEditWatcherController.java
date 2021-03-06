package com.nwchecker.server.controller;

import com.nwchecker.server.exceptions.ContestAccessDenied;
import com.nwchecker.server.service.ContestEditWatcherService;
import com.nwchecker.server.service.ContestService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.security.Principal;

/**
 * <h1>Contest Edit Watcher Controller</h1>
 * This spring controller contains mapped methods, that used
 * to synchronize teachers access and prevent simultaneous editing
 * of the same contest by multiple users.
 * <p>
 *
 * @author Roman Zayats
 * @version 1.0
 * @since 2015-02-21
 */
@Controller("ContestEditWatcherController")
public class ContestEditWatcherController {

    private static final Logger LOG
            = Logger.getLogger(ContestEditWatcherController.class);
    @Autowired
    private ContestEditWatcherService contestEditWatcherService;

    @Autowired
    private ContestService contestService;

    private static final long CONTEST_EDIT_TIME_OUT_POLLING = 10000;

    private static final long REQUEST_CONTEST_STILL_EDIT_TIME_OUT = 1500;

    /**
     * This mapped method used to indicate if Contest is currently editing by someone.
     * <p>
     *
     * @param principal This is general information about user, who
     *                  tries to call this method
     * @param id ID of contest
     */
    @RequestMapping("/checkContestEdit")
    @ResponseBody
    public DeferredResult<String> checkIfContestEditing(Principal principal, @RequestParam(value = "id") final int id) {
        //check if user has access:
        if (!contestService.checkIfUserHaveAccessToContest(principal.getName(), id)) {
            throw new ContestAccessDenied(principal.getName() + " tried to edit Contest. Access denied.");
        }
        //create asynchronous response:
        final DeferredResult<String> deferredResult = new DeferredResult<>(REQUEST_CONTEST_STILL_EDIT_TIME_OUT, "OK");
        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                //on execute delete himself from map:
                contestEditWatcherService.removeRequestStillContestEditing(id);
            }
        });

        //who is currently editing contest:
        String result = contestEditWatcherService.isEditing(id);
        //if nobody currently edit contest, or current user is editing contest:
        if ((result == null) || (result != null && result.equals(principal.getName()))) {
            deferredResult.setResult("OK");
            return deferredResult;
        } else {
            //if somebody other is editing contest:
            //write to log:
            LOG.debug("User(" + principal.getName() + ") tries to check if Contest(id=" + id +
                    ") is still editing by User(" + result + ")");
            //send him timeOut and check if he will reconnect:
            contestEditWatcherService.setDeferredResult(id, "timeOut");
            //put asynchronous response to request map:
            contestEditWatcherService.addRequestStillContestEditing(id, deferredResult);
            return deferredResult;
        }
    }

    /**
     * This mapped method used to set Contest editing by user.
     * <p>
     *
     * @param principal This is general information about user, who
     *                  tries to call this method
     * @param id ID of contest
     */
    @RequestMapping("/editingContest")
    @ResponseBody
    public DeferredResult<String> editingContest(Principal principal, @RequestParam(value = "id") final int id) {
        //check if user has access:
        if (!contestService.checkIfUserHaveAccessToContest(principal.getName(), id)) {
            throw new ContestAccessDenied(principal.getName() + " tried to edit Contest. Access denied.");
        }
        //create asynchronous response object:
        final DeferredResult<String> deferredResult = new DeferredResult<>(CONTEST_EDIT_TIME_OUT_POLLING, "timeOut");
        //on time out- delete "currentlyEditing" from contestService map:
        deferredResult.onCompletion(new Runnable() {
            @Override
            public void run() {
                contestEditWatcherService.removeContestEditingUser(id);
            }
        });
        //check if contest is currently editing:
        String username = contestEditWatcherService.isEditing(id);
        //if editing by current user:
        if (username != null && username.equals(principal.getName())) {
            //remove old request:
            contestEditWatcherService.removeContestEditingUser(id);
            //add new:
            contestEditWatcherService.addContestEditingUser(id, principal.getName(), deferredResult);
        } else if (username != null && !username.equals(principal.getName())) {
            deferredResult.setResult(username);
            return deferredResult;
        } else if (username == null) {
            //if nobody is editing- then set that current user is editing:
            contestEditWatcherService.addContestEditingUser(id, principal.getName(), deferredResult);
        }
        return deferredResult;
    }
}
