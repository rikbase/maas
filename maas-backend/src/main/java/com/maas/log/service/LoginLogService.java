package com.maas.log.service;

import com.maas.log.dto.LoginLogVO;
import com.maas.log.dto.LoginStatsVO;
import com.maas.log.entity.LoginLog;
import com.maas.log.repository.LoginLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class LoginLogService {
    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    public void record(String username, String ip, String userAgent, boolean success, String failReason) {
        LoginLog log = new LoginLog(username, ip, userAgent, success, failReason);
        loginLogRepository.save(log);
    }

    public List<LoginLogVO> getRecent() {
        return loginLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
            .map(LoginLogVO::from)
            .toList();
    }

    public LoginStatsVO getStats() {
        Instant todayStart = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant last24h = Instant.now().minusSeconds(86400);

        int todaySuccess = loginLogRepository.countByCreatedAtAfterAndSuccessTrue(todayStart);
        int todayFailed = loginLogRepository.countByCreatedAtAfterAndSuccessFalse(todayStart);
        int last24hAttempts = loginLogRepository.countByCreatedAtAfter(last24h);

        return new LoginStatsVO(todaySuccess, todayFailed, last24hAttempts);
    }
}
