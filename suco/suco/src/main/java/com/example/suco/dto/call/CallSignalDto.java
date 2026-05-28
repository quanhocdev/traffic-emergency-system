package com.example.suco.dto.call;

public class CallSignalDto {
    private String type;
    private String from;
    private String to;
    private String sdp;
    private Object candidate; 

    public CallSignalDto() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSdp() { return sdp; }
    public void setSdp(String sdp) { this.sdp = sdp; }

    public Object getCandidate() { return candidate; }
    public void setCandidate(Object candidate) { this.candidate = candidate; }
}