import java.io.*;
import java.util.ArrayList;

class InputFileReader {
    private String algorithm;
    private int processCount;
    private ArrayList<ProcessData> processes;
    private int timeQuantum;

    public void readInputFile(String fileName) {
        processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // First line is the scheduling algorithm
            String firstLine = br.readLine().trim();
            String[] parts = firstLine.split(" ");

            // Assume the first part is the algorithm name
            algorithm = parts[0];

            // If there’s a time quantum specified, parse it as an integer
            timeQuantum = parts.length > 1 ? Integer.parseInt(parts[1]) : 0; // default to 0 if not specified

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

    public int getTimeQuantum() {
        return timeQuantum;
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

    public void setBurstTime(int updatedBurstTime) {
        this.burstTime = updatedBurstTime;
    }

    // Override toString to provide custom output
    @Override
    public String toString() {
        return "Process ID: " + processID + ", Arrival Time: " + arrivalTime
                + ", Burst Time: " + burstTime + ", Priority: " + priority;
    }
}
// input formatting
// PR_withPREMP -- algo name
// 3 -- number of processes
// 1 0 8 3 -- process #, arrival time, cpu burst length, priority #
// 2 3 1 1
// 3 5 2 4
// smaller priority # is higher prioirity

public class CPUSchedulingAlgo {
    public static final String INPUT = "input.txt";
    public static final String OUTPUT = "output.txt";

    public static void main(String[] args) {
        InputFileReader inputReader = new InputFileReader();
        inputReader.readInputFile(INPUT);

        String algo = inputReader.getAlgorithm();
        ArrayList<ProcessData> processes = inputReader.getProcesses();
        for (ProcessData process : processes) {
            System.out.println(process);
        }

        OutputFileWriter outputWriter = new OutputFileWriter();
        ArrayList<String> outputLines = new ArrayList<>();
        double averageWaitingTime = 0.0;

        switch (algo) {
            case "RR":
                int timeQuantum = (int) inputReader.getTimeQuantum();
                outputLines.add("Round Robin (Time Quantum: " + timeQuantum + ")");
                averageWaitingTime = roundRobin(processes, timeQuantum, outputLines);
                break;
            case "SJF":
                outputLines.add("Shortest Job First");
                averageWaitingTime = shortestJobFirst(processes, outputLines);
                break;
            case "PR_noPREMP":
                outputLines.add("Priority Scheduling (No Preemption)");
                averageWaitingTime = priorityNoPreempt(processes, outputLines);
                break;
            case "PR_withPREMP":
                outputLines.add("Priority Scheduling (With Preemption)");
                averageWaitingTime = priorityWithPreempt(processes, outputLines);
                break;
            default:
                System.out.println("Error with input type: " + algo + "\nNot a valid type");
        }

        outputWriter.writeOutputFile(OUTPUT, outputLines, averageWaitingTime);
    }

    public static double roundRobin(ArrayList<ProcessData> processes, int timeQuantum, ArrayList<String> outputLines) {
        int numDone = 0, timer = 0, nextArrival = 0, totalWait = 0;
        ArrayList<ProcessData> readyQueue = new ArrayList<>();

        // Sort processes by arrival time
        processes.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

        timer = processes.get(0).getArrivalTime();

        // Add processes to readyQueue until initial timer
        while (nextArrival < processes.size() && processes.get(nextArrival).getArrivalTime() <= timer) {
            readyQueue.add(processes.get(nextArrival++));
        }

        while (numDone != processes.size()) {
            if (readyQueue.isEmpty()) {
                // Advance time to the arrival of the next process if the queue is empty
                timer = processes.get(nextArrival).getArrivalTime();
                readyQueue.add(processes.get(nextArrival++));
            }

            // Execute the current process
            ProcessData executingProc = readyQueue.remove(0); // remove the first process
            outputLines.add(timer + "\t" + executingProc.getProcessID());

            int currProcessBurst = executingProc.getBurstTime();
            int timeIncrement = Math.min(currProcessBurst, timeQuantum);// shorter burst or shorter time quantum

            // Update burst time and timer
            executingProc.setBurstTime(currProcessBurst - timeIncrement);
            timer += timeIncrement;

            // Update waiting time for remaining processes in the queue
            totalWait += readyQueue.size() * timeIncrement;

            // Add any newly arrived processes to the readyQueue
            while (nextArrival < processes.size() && processes.get(nextArrival).getArrivalTime() <= timer) {
                // add the partial wait times
                totalWait += timer - processes.get(nextArrival).getArrivalTime();
                readyQueue.add(processes.get(nextArrival++));
            }

            // If the process has finished, increment numDone, else add it back to the end
            // of the queue
            if (executingProc.getBurstTime() == 0) {
                numDone++;
            } else {
                readyQueue.add(executingProc); // Re-add to the end of the queue if not done
            }

        }

        return (double) totalWait / processes.size();
    }

    // Shortest Job First without preemption
    public static double shortestJobFirst(ArrayList<ProcessData> processes, ArrayList<String> outputLines) {
        int numDone = 0, timer = 0, totalWait = 0;
        ArrayList<ProcessData> readyQueue = new ArrayList<>();

        // Sort processes by arrival time
        processes.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

        // Track which processes are added to the queue
        int nextArrival = 0;

        timer = processes.get(0).getArrivalTime();

        // Add processes to the ready queue as they arrive
        while (numDone < processes.size()) {
            // Add all processes that have arrived by the current timer value
            while (nextArrival < processes.size() && processes.get(nextArrival).getArrivalTime() <= timer) {
                readyQueue.add(processes.get(nextArrival));
                nextArrival++;
            }

            // Sort ready queue by burst time (Shortest Job First)
            if (!readyQueue.isEmpty()) {
                readyQueue.sort((p1, p2) -> Integer.compare(p1.getBurstTime(), p2.getBurstTime()));
                ProcessData currentProcess = readyQueue.remove(0);

                // add the wait time for the current process
                int waitTime = timer - currentProcess.getArrivalTime();
                totalWait += waitTime;

                String output = timer + "\t" + currentProcess.getProcessID();
                outputLines.add(output);

                // Update timer
                timer += currentProcess.getBurstTime();

                numDone++;
            } else {
                // Increment the timer if no process is in the ready queue
                timer++;
            }
        }

        double averageWaitingTime = (double) totalWait / processes.size();
        return averageWaitingTime;
    }

    // Priority No Preemption Scheduling
    public static double priorityNoPreempt(ArrayList<ProcessData> processes, ArrayList<String> outputLines) {
        int numDone = 0, timer = 0, currProc = 0, nextArrival = 0, timeIncrement = 0, totalWait = 0;
        ArrayList<ProcessData> readyQueue = new ArrayList<>();

        // Sort processes by arrival time
        processes.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

        timer = processes.get(0).getArrivalTime();

        // Add processes to readyQueue until initial timer
        while (nextArrival < processes.size() && processes.get(nextArrival).getArrivalTime() <= timer) {
            readyQueue.add(processes.get(nextArrival++));
        }

        while (numDone != processes.size()) {
            timeIncrement = 0;

            // Find the process with the highest priority (lowest priority number)
            currProc = 0;
            for (int i = 1; i < readyQueue.size(); i++) {
                if (readyQueue.get(i).getPriority() < readyQueue.get(currProc).getPriority()) {
                    currProc = i;
                }
            }

            // Output current process execution start
            ProcessData executingProc = readyQueue.get(currProc);
            outputLines.add(timer + "\t" + executingProc.getProcessID());
            timeIncrement = executingProc.getBurstTime();

            // Update timer and total wait time
            timer += timeIncrement;
            totalWait += (readyQueue.size() - 1) * timeIncrement;

            // Remove finished process from readyQueue
            readyQueue.remove(currProc);
            numDone++;

            // Add arriving processes to the readyQueue
            while (nextArrival < processes.size() && processes.get(nextArrival).getArrivalTime() <= timer) {
                readyQueue.add(processes.get(nextArrival++));
                totalWait += timer - processes.get(nextArrival - 1).getArrivalTime();
            }
        }

        double averageWaitingTime = (double) totalWait / processes.size();
        return averageWaitingTime;
    }

    public static double priorityWithPreempt(ArrayList<ProcessData> processes, ArrayList<String> outputLines) {
        int numDone = 0, timer = 0, nextArrival = 0, timeIncrement = 0, totalWait = 0;
        int currProc = -1;
        ArrayList<ProcessData> readyQueue = new ArrayList<>();
        boolean needDecision = true;

        // Sort processes by arrival time
        processes.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

        // Initialize the timer with the arrival time of the first process
        timer = processes.get(0).getArrivalTime();

        // Add initial processes to readyQueue based on initial timer value
        while (nextArrival < processes.size() && processes.get(nextArrival).getArrivalTime() <= timer) {
            readyQueue.add(processes.get(nextArrival++));
        }

        while (numDone != processes.size()) {
            timeIncrement = 0;

            // Add any newly arriving processes to readyQueue and check if a new scheduling
            // decision is needed
            for (int arriving = nextArrival; arriving < processes.size()
                    && processes.get(arriving).getArrivalTime() <= timer; arriving++) {
                ProcessData newProcess = processes.get(arriving);
                readyQueue.add(newProcess);
                nextArrival++;
                // Require a new decision if the arriving process has a higher priority than the
                // current or its the initital decision or last process completed
                if (currProc == -1 || newProcess.getPriority() < readyQueue.get(currProc).getPriority()) {
                    needDecision = true;
                }
            }

            // If a new scheduling decision is needed
            if (needDecision) {
                currProc = 0;
                for (int i = 1; i < readyQueue.size(); i++) {
                    // Select process with the highest priority, tiebreaker by arrival time
                    if (readyQueue.get(i).getPriority() < readyQueue.get(currProc).getPriority() ||
                            (readyQueue.get(i).getPriority() == readyQueue.get(currProc).getPriority() &&
                                    readyQueue.get(i).getArrivalTime() < readyQueue.get(currProc).getArrivalTime())) {
                        currProc = i;
                    }
                }
                outputLines.add(timer + "\t" + readyQueue.get(currProc).getProcessID());
                needDecision = false;
            }

            ProcessData executingProc = readyQueue.get(currProc);
            if (nextArrival >= processes.size()
                    || (executingProc.getBurstTime() + timer) <= processes.get(nextArrival).getArrivalTime()) {
                // No new arrivals during the current process's remaining burst time
                timeIncrement = executingProc.getBurstTime();
                executingProc.setBurstTime(0);
                numDone++;
                readyQueue.remove(currProc);
                currProc = -1; // Reset current process
                needDecision = true;
            } else {
                timeIncrement = processes.get(nextArrival).getArrivalTime() - timer;
                executingProc.setBurstTime(executingProc.getBurstTime() - timeIncrement);
            }

            timer += timeIncrement;

            // Calculate wait time for all other processes in the readyQueue
            if (currProc == -1) {
                totalWait += (readyQueue.size()) * timeIncrement;
            } else {
                totalWait += (readyQueue.size() - 1) * timeIncrement;
            }

        }
        double averageWaitingTime = (double) totalWait / processes.size();
        return averageWaitingTime;
    }

}
