package com.example.suco.service.suco.baocao.system.reward;

public class DuplicateReportRewardPolicy implements BaoCaoRewardPolicy {

    public int normalPoint() { return 2; }

    public int vipPoint() { return 5; }

    public String reason() {
        return "DUPLICATE_REPORT";
    }
}