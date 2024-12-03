
/*
This requires the fuile to be named input.txt and require it to be in the same directory and genereates output.txt in the same directory
*/
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class MemoryManagement {

    public static void main(String[] args) {
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            // Initialize the reader and writer
            reader = new BufferedReader(new FileReader("input.txt"));
            writer = new BufferedWriter(new FileWriter("output.txt"));

            // Read the first line from the file
            String firstLine = reader.readLine();
            String[] values = firstLine.split("\\s+");

            // Parse the first line values
            int numPages = Integer.parseInt(values[0]);
            int numFrames = Integer.parseInt(values[1]);
            int numPageReq = Integer.parseInt(values[2]);

            // Initialize the list of page requests
            ArrayList<Integer> list = new ArrayList<>(numPageReq);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pageRequests = line.split("\\s+");
                for (String page : pageRequests) {
                    list.add(Integer.parseInt(page));
                }
            }

            // Redirect System.out to BufferedWriter for output.txt for easier file write
            PrintWriter printWriter = new PrintWriter(writer);

            // Calling the Replacement Policies
            FIFO(list, numPages, numFrames, numPageReq, printWriter);
            printWriter.println();
            Optimal(list, numPages, numFrames, numPageReq, printWriter);
            printWriter.println();
            LRU(list, numPages, numFrames, numPageReq, printWriter);

            // Close resources
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // FIFO Policy
    public static void FIFO(ArrayList<Integer> list, int numPages, int numFrames, int numPageReq, PrintWriter writer) {
        writer.println("FIFO");
        ArrayList<Integer> newFrames = new ArrayList<>(numFrames);

        int count = -1;
        int faults = 0;
        int frameIdx = 0;
        int curPage;

        while (++count != numPageReq) {
            curPage = list.get(count);
            if (frameIdx == numFrames)
                frameIdx = 0; // Reset the frame index.
            // Page is not already loaded.
            if (!newFrames.contains(curPage) && count <= numFrames) {
                newFrames.add(curPage);
                writer.println("Page " + curPage + " loaded in Frame " + frameIdx);
                frameIdx++;
                faults++;
            }
            // Page is not already loaded and hasn't empty space.
            else if (!newFrames.contains(curPage) && count > numFrames) {
                writer.println("Page " + newFrames.get(frameIdx) + " unloaded from Frame " + frameIdx + ", Page "
                        + curPage + " loaded into Frame " + frameIdx);
                newFrames.set(frameIdx, curPage);
                frameIdx++;
                faults++;
            }
            // Page is already loaded.
            else
                writer.println("Page " + curPage + " already in Frame " + newFrames.indexOf(curPage));
        }
        writer.println(faults + " page faults");
    }
    // End of FIFO

    // LRU Policy
    public static void LRU(ArrayList<Integer> list, int numPages, int numFrames, int numPageReq, PrintWriter writer) {
        writer.println("LRU");

        ArrayList<Integer> newFrames = new ArrayList<>(numFrames);
        ArrayList<Integer> frameFreq = new ArrayList<>(numFrames);
        ArrayList<Integer> rangeArray = new ArrayList<>();

        int count = -1;
        int faults = 0;
        int frameIdx = 0;
        int curPage, minIdx;

        while (++count != numPageReq) {
            curPage = list.get(count);
            rangeArray.add(curPage);
            if (frameIdx == numFrames)
                frameIdx = 0; // Reset frame index.
            // Page is not loaded.
            if (!newFrames.contains(curPage) && count <= numFrames) {
                newFrames.add(curPage);
                writer.println("Page " + curPage + " loaded into Frame " + frameIdx);
                frameIdx++;
                faults++;
            }
            // Page is not already loaded and hasn't empty space.
            else if (!newFrames.contains(curPage) && count > numFrames) {
                for (int frame : newFrames)
                    frameFreq.add(rangeArray.lastIndexOf(frame));
                minIdx = frameFreq.indexOf(Collections.min(frameFreq));
                writer.println("Page " + newFrames.get(minIdx) + " unloaded from Frame " + minIdx + ", Page "
                        + curPage + " loaded in Frame " + minIdx);
                newFrames.set(minIdx, curPage);
                frameFreq.clear();
                faults++;
            }
            // Page is already loaded.
            else
                writer.println("Page " + curPage + " already in Frame " + newFrames.indexOf(curPage));
        }
        writer.println(faults + " page faults");
    }
    // End of LRU

    // Optimal Policy
    public static void Optimal(ArrayList<Integer> list, int numPages, int numFrames, int numPageReq,
            PrintWriter writer) {
        writer.println("Optimal");

        ArrayList<Integer> newFrames = new ArrayList<>(numFrames);
        ArrayList<Integer> frameFreq = new ArrayList<>(numFrames);
        ArrayList<Integer> rangeArray = new ArrayList<>();

        int count = -1;
        int faults = 0;
        int frameIdx = 0;
        int curPage, minIdx;

        while (++count != numPageReq) {
            curPage = list.get(count);
            for (int i = count; i != numPageReq; i++)
                rangeArray.add(list.get(i));
            if (frameIdx == numFrames)
                frameIdx = 0; // Reset the frame index.
            // Page is not loaded.
            if (!newFrames.contains(curPage) && count <= numFrames) {
                newFrames.add(curPage);
                writer.println("Page " + curPage + " loaded in Frame " + frameIdx);
                frameIdx++;
                faults++;
            }
            // Page is not already loaded and hasn't empty space.
            else if (!newFrames.contains(curPage) && count > numFrames) {
                for (int frame : newFrames)
                    frameFreq.add(rangeArray.lastIndexOf(frame));
                minIdx = frameFreq.indexOf(Collections.min(frameFreq));
                writer.println("Page " + newFrames.get(minIdx) + " unloaded from Frame " + minIdx + ", Page "
                        + curPage + " loaded into Frame " + minIdx);
                newFrames.set(minIdx, curPage);
                frameFreq.clear();
                faults++;
            }
            // Page is already loaded.
            else
                writer.println("Page " + curPage + " already in Frame " + newFrames.indexOf(curPage));
            rangeArray.clear();
        }
        writer.println(faults + " page faults");
    }
    // End of Optimal
}
