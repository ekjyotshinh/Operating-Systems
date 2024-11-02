import java.io.*;
import java.util.ArrayList;

class InputFileReader {
    private String algorithm;
    private int processCount;
    private ArrayList<ProcessData> processes;

    public void readInputFile(String fileName) {
        processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // First line is the scheduling algorithm
            algorithm = br.readLine().trim();
            // Second line is the number of processes
            processCount = Integer.parseInt(br.readLine().trim());

            // Read each process line
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                int processID = Integer.parseInt(tokens[0]);
                int arrivalTime = Integer.parseInt(tokens[1]);
                int burstTime = Integer.parseInt(tokens[2]);
                int priority = Integer.parseInt(tokens[3]);

                // Instantiate ProcessData and add to the list
                processes.add(new ProcessData(processID, priority, arrivalTime, burstTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter methods to retrieve read data
    public String getAlgorithm() {
        return algorithm;
    }

    public ArrayList<ProcessData> getProcesses() {
        return processes;
    }

    public int getProcessCount() {
        return processCount;
    }
}

class OutputFileWriter {
    public void writeOutputFile(String fileName, ArrayList<String> outputLines, double averageWaitingTime) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : outputLines) {
                bw.write(line);
                bw.newLine();
            }
            bw.write("AVG Waiting Time: " + String.format("%.2f", averageWaitingTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

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
