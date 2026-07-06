package com.example.suco.service.suco.baocao.system.reward;

public class NewReportRewardPolicy implements BaoCaoRewardPolicy {

    public int normalPoint() { return 5; }

    public int vipPoint() { return 10; }

    public String reason() {
        return "NEW_REPORT";
    }
}