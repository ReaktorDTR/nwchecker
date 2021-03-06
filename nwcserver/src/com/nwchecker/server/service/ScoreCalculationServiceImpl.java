package com.nwchecker.server.service;

import com.nwchecker.server.dao.ContestPassDAO;
import com.nwchecker.server.model.Contest;
import com.nwchecker.server.model.ContestPass;
import com.nwchecker.server.model.TaskPass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScoreCalculationServiceImpl implements ScoreCalculationService {
    @Autowired
    private ContestService contestService;
    @Autowired
    private ContestPassService contestPassService;

    @Autowired
    private ContestPassDAO contestPassDAO;

    @Override
    @Transactional
    public void calculateScore(int ContestId) {
        Contest contest = contestService.getContestByID(ContestId);
        //get all user ContestPasses for this contest:
        List<ContestPass> allContestPasses = contestPassService.getContestPasses(ContestId);

        //check each ContestPass for each user:
        for (ContestPass contestPass : allContestPasses) {
            Set<Integer> passedTasks = new HashSet<>();
            //go throw each taskPass:
            for (TaskPass taskPass : contestPass.getTaskPassList()) {
                //if passed firs time:
                if (taskPass.isPassed() && !passedTasks.contains(taskPass.getTask().getId())) {
                    passedTasks.add(taskPass.getTask().getId());
                    //increment passed count for this contestPass:
                    contestPass.setPassedCount(contestPass.getPassedCount() + 1);
                    //add penalty time:
                    contestPass.setTimePenalty(contestPass.getTimePenalty() + taskPass.getPassedMinute());

                    //search all failed taskPassed for this Task id:
                    for (int i = 0; i < contestPass.getFailedTaskPasses(taskPass.getTask().getId()).size(); i++) {
                        //for each failed taskPass increment by 20 minute penalty time:
                        contestPass.setTimePenalty(contestPass.getTimePenalty() + 20);
                    }
                    contestPassDAO.updateContestPass(contestPass);
                }
            }
        }
        Collections.sort(allContestPasses);
        int i = 1;
        for (ContestPass c : allContestPasses) {
            c.setRank(i);
            contestPassDAO.updateContestPass(c);
            i++;
        }
    }
}
