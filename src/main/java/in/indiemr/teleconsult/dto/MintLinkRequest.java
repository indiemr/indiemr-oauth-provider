package in.indiemr.teleconsult.dto;

public class MintLinkRequest {
    private String providerUuid;
    private String providerDisplay;
    private String patientName;
    private String patientPhone;
    private String oauthProviderCode = "GOOGLE";
    private String appointmentUuid; 

    public String getProviderUuid() { return providerUuid; }
    public void setProviderUuid(String providerUuid) { this.providerUuid = providerUuid; }
    public String getProviderDisplay() { return providerDisplay; }
    public void setProviderDisplay(String providerDisplay) { this.providerDisplay = providerDisplay; }
    public String getOauthProviderCode() { return oauthProviderCode; }
    public void setOauthProviderCode(String oauthProviderCode) { this.oauthProviderCode = oauthProviderCode; }
    public String getAppointmentUuid() { return appointmentUuid; }
    public void setAppointmentUuid(String appointmentUuid) { this.appointmentUuid = appointmentUuid; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
}
