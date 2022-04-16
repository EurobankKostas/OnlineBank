package newbank.server.microloan;

import java.util.List;

public class MicroloanManagement {

    private List<MicroLoanRequest> microLoanRequestList;
    private List<Microloan> microloanList;

    public List<MicroLoanRequest> getMicroLoanRequestList() {
        return microLoanRequestList;
    }

    public void setMicroLoanRequestList(List<MicroLoanRequest> microLoanRequestList) {
        this.microLoanRequestList = microLoanRequestList;
    }

    public List<Microloan> getMicroloanList() {
        return microloanList;
    }

    public void setMicroloanList(List<Microloan> microloanList) {
        this.microloanList = microloanList;
    }
}
