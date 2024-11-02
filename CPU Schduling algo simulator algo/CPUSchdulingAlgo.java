
// ProcessData class to store process information
class ProcessData {
    private int processID;
    private int priority;
    private int arrivalTime;
    private int burstTime;

    public ProcessData(int processID, int priority, int arrivalTime, int burstTime) {
        this.processID = processID;
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
    }

    // Getters for process properties
    public int getProcessID() {
        return processID;
    }

    public int getPriority() {
        return priority;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }
}

public class CPUSchdulingAlgo {

}
