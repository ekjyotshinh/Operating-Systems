/*
CSC139 
Fall 2024
First Assignment
Shinh, Ekjyot Singh
Section 01
OSs Tested on Linux & Mac.
*/

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <string.h>

// Size of shared memory block
// Pass this to ftruncate and mmap
#define SHM_SIZE 4096

// Global pointer to the shared memory block
// This should receive the return value of mmap
// Don't change this pointer in any function
void* gShmPtr;

// You won't necessarily need all the functions below
void SetIn(int);
void SetOut(int);
void SetHeaderVal(int, int);
int GetBufSize();
int GetItemCnt();
int GetIn();
int GetOut();
int GetHeaderVal(int);
void WriteAtBufIndex(int, int);
int ReadAtBufIndex(int);

int main()
{
    const char *name = "OS_HW1_Ekjyot"; // Name of shared memory block to be passed to shm_open
    int bufSize; // Bounded buffer size
    int itemCnt; // Number of items to be consumed
    int in; // Index of next item to produce
    int out; // Index of next item to consume
     
    // Write code here to create a shared memory block and map it to gShmPtr
    int shm_fd = shm_open(name, O_RDWR, 0666);

    gShmPtr = mmap(0, SHM_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);	

    // Write code here to read the four integers from the header of the shared memory block 
    bufSize = GetBufSize();
    itemCnt = GetItemCnt();
    in = GetIn();
    out = GetOut();
 
    int i = 0; // Loop counter.

    // Loop until we've read all the items.	
    while(i < itemCnt) {
		
	in = GetIn(); // Get in from header.

	// If the buffer is not empty.
	if(in != out) {
	    int val = ReadAtBufIndex(out); // Read value at buffer index.
	    printf("Consuming Item %d with value %d at Index %d\n", i, val, out); // Print stuff so we know it worked.
	    out = (out + 1) % bufSize; // Increase the index, mod with the buffer size.
	    SetOut(out); // Set the header to the new buffer index for output.
	    i++; // Increase the loop counter.
	}
    }               
          
     shm_unlink(name);

     return 0;
}


// Set the value of shared variable "in"
void SetIn(int val)
{
        SetHeaderVal(2, val);
}

// Set the value of shared variable "out"
void SetOut(int val)
{
        SetHeaderVal(3, val);
}

// Get the ith value in the header
int GetHeaderVal(int i)
{
        int val;
        void* ptr = gShmPtr + i*sizeof(int);
        memcpy(&val, ptr, sizeof(int));
        return val;
}

// Set the ith value in the header
void SetHeaderVal(int i, int val)
{
        void* ptr = gShmPtr + i*sizeof(int);
        memcpy(ptr, &val, sizeof(int));
}

// Get the value of shared variable "bufSize"
int GetBufSize()
{       
        return GetHeaderVal(0);
}

// Get the value of shared variable "itemCnt"
int GetItemCnt()
{
        return GetHeaderVal(1);
}

// Get the value of shared variable "in"
int GetIn()
{
        return GetHeaderVal(2);
}

// Get the value of shared variable "out"
int GetOut()
{             
        return GetHeaderVal(3);
}


// Write the val at the given index in the bounded buffer 
void WriteAtBufIndex(int indx, int val)
{
        // Skip the four-integer header and go to the given index
        void* ptr = gShmPtr + 4*sizeof(int) + indx*sizeof(int);
        memcpy(ptr, &val, sizeof(int));
}

// Read the val at the given index in the bounded buffer
int ReadAtBufIndex(int indx)
{
        int val;
 
        // Skip the four-integer header and go to the given index
        void* ptr = gShmPtr + 4*sizeof(int) + indx*sizeof(int);
        memcpy(&val, ptr, sizeof(int));
        return val;
}
