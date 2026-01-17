package io.github.sjfailure.kccommunityconnect;

public class ServiceEvent {

    private final String providerName;
    private final String serviceCategory;
    private final String serviceType;
    private final String startTime;

    public ServiceEvent(String providerName, String serviceCategory, String serviceType, String startTime) {
        this.providerName = providerName;
        this.serviceCategory = serviceCategory;
        this.serviceType = serviceType;
        this.startTime = startTime;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getStartTime() {
        return startTime;
    }
}
