/*
Name - Ekjyot Shinh
Section - 01
OS tested on is Linux
*/

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/timeb.h>
#include <semaphore.h>
#include <stdbool.h>


#define MAX_SIZE 100000000
#define MAX_THREADS 16
#define RANDOM_SEED 7649
#define MAX_RANDOM_NUMBER 3000
#define NUM_LIMIT 9973

// Global variables
long gRefTime; //For timing
int gData[MAX_SIZE]; //The array that will hold the data

int gThreadCount; //Number of threads
int gDoneThreadCount; //Number of threads that are done at a certain point. Whenever a thread is done, it increments this. Used with the semaphore-based solution
int gThreadProd[MAX_THREADS]; //The modular product for each array division that a single thread is responsible for
bool gThreadDone[MAX_THREADS]; //Is this thread done? Used when the parent is continually checking on child threads

// Semaphores
sem_t completed; //To notify parent that all threads have completed or one of them found a zero
sem_t mutex; //Binary semaphore to protect the shared variable gDoneThreadCount

int SqFindProd(int size); //Sequential FindProduct (no threads) computes the product of all the elements in the array mod NUM_LIMIT
void *ThFindProd(void *param); //Thread FindProduct but without semaphores
void *threadFunction(void *param); //Thread FindProduct but without semaphores
void *ThFindProdWithSemaphore(void *param); //Thread FindProduct with semaphores
int ComputeTotalProduct(); // Multiply the division products to compute the total modular product 
void InitSharedVars();
void GenerateInput(int size, int indexForZero); //Generate the input array
void CalculateIndices(int arraySize, int thrdCnt, int indices[MAX_THREADS][3]); //Calculate the indices to divide the array into T divisions, one division per thread
int GetRand(int min, int max);//Get a random number between min and max

//Timing functions
long GetMilliSecondTime(struct timeb timeBuf);
long GetCurrentTime(void);
void SetTime(void);
long GetTime(void);

int main(int argc, char *argv[]){

	pthread_t tid[MAX_THREADS];
	pthread_attr_t attr[MAX_THREADS];
	int indices[MAX_THREADS][3];
	int i, indexForZero, arraySize, prod;

	// Code for parsing and checking command-line arguments
	if(argc != 4){
		fprintf(stderr, "Invalid number of arguments!\n");
		exit(-1);
	}
	if((arraySize = atoi(argv[1])) <= 0 || arraySize > MAX_SIZE){
		fprintf(stderr, "Invalid Array Size\n");
		exit(-1);
	}
	gThreadCount = atoi(argv[2]);
	if(gThreadCount > MAX_THREADS || gThreadCount <=0){
		fprintf(stderr, "Invalid Thread Count\n");
		exit(-1);
	}
	indexForZero = atoi(argv[3]);
	if(indexForZero < -1 || indexForZero >= arraySize){
		fprintf(stderr, "Invalid index for zero!\n");
		exit(-1);
	}

    GenerateInput(arraySize, indexForZero);

    CalculateIndices(arraySize, gThreadCount, indices);

	// Code for the sequential part
	SetTime();
	prod = SqFindProd(arraySize);
	printf("Sequential multiplication completed in %ld ms. Product = %d\n", GetTime(), prod);

	// Threaded with parent waiting for all child threads
	InitSharedVars();
	SetTime();


	// Don't forget to properly initialize shared variables

	// Initialize threads, create threads, and then let the parent wait for all threads using pthread_join
	/*
    int pthread_attr_init(pthread_attr_t *attr);
    attr: A pointer to a pthread_attr_t structure that will be initialized with default attributes.

    int pthread_create(pthread_t *thread, const pthread_attr_t *attr, 
                   void *(*start_routine)(void *), void *arg);
    pthread_t *thread: A pointer to a pthread_t variable where the thread ID of the new thread will be stored.
    const pthread_attr_t *attr: A pointer to a thread attributes object (initialized by pthread_attr_init). If you want to use default attributes, you can pass NULL.
    void *(*start_routine)(void *): A pointer to the function that will be executed by the new thread. This function should take a single void * argument and return a void *.
    void *arg: A single argument that will be passed to the start_routine function. This can be NULL if no arguments are needed.
	*/
	for(i = 0; i < gThreadCount; i++) {
		pthread_attr_init(&attr[i]);
		pthread_create(&tid[i], &attr[i], ThFindProd, (void*)indices[i]);
	}

    /*
    int pthread_join(pthread_t thread, void **retval);
    pthread_t thread: The thread identifier of the thread to wait for.
    void **retval: A pointer to a pointer where the return value of the terminated thread will be stored. If you don’t need the return value, you can pass NULL.
    */
	for(i = 0; i < gThreadCount; i++) {
		pthread_join(tid[i], NULL);
	}


    prod = ComputeTotalProduct();
	printf("Threaded multiplication with parent waiting for all children completed in %ld ms. Product = %d\n", GetTime(), prod);

	// Multi-threaded with busy waiting (parent continually checking on child threads without using semaphores)
	InitSharedVars();
	SetTime();


	// Initialize threads, create threads, and then make the parent continually check on all child threads
	// The thread start function is ThFindProd
	// Don't forget to properly initialize shared variables


	// Initialize threads and start busy waiting
	for(i = 0; i < gThreadCount; i++) {
		pthread_create(&tid[i], &attr[i], ThFindProd, (void*)indices[i]);
	}

	// Parent busy waits until all threads are done
	bool allDone;
    bool zeroFound = false;

    do {
        allDone = true;
        for (int i = 0; i < gThreadCount; i++) {
            if (gThreadDone[i]) {
                if (gThreadProd[i] == 0) {
                    zeroFound = true;  // A zero product has been found
                    printf("breaking at%ld ms. Product = %d\n", GetTime(), prod);
                    break;  // No need to continue checking other threads
                }
            }else{
                allDone = false;  // At least one thread is still working
            }    
        }
        if (zeroFound) {
            break;
        }

    } while (!allDone);

    if (zeroFound) {
    prod = 0;
    } else{
    prod = ComputeTotalProduct();
    }
    printf("Threaded multiplication with parent continually checking on children completed in %ld ms. Product = %d\n", GetTime(), prod);


	// Multi-threaded with semaphores

	InitSharedVars();
    // Initialize your semaphores here

	sem_init(&completed, 0, 0);
    sem_init(&mutex, 0, 1);

	SetTime();

    // Write your code here
	// Initialize threads, create threads, and then make the parent wait on the "completed" semaphore
	// The thread start function is ThFindProdWithSemaphore
	// Don't forget to properly initialize shared variables and semaphores using sem_init

// Create threads

    for(i = 0; i < gThreadCount; i++) {
    	pthread_create(&tid[i], &attr[i], ThFindProdWithSemaphore, (void*)indices[i]);
    }

    // Parent waits on the semaphore until either a zero is found or all threads are done
    zeroFound = false;

    // Parent waits for a signal from any thread
 
    sem_wait(&completed);
    // Check if any thread found a zero
    for (int i = 0; i < gThreadCount; i++) {
        if (gThreadProd[i] == 0) {
            zeroFound = true;
            break; // Exit the loop early if a zero is found
        }
    }
 

    if (!zeroFound) {
        // Compute the total product if no zero was found
        prod = ComputeTotalProduct();

    } else {
        prod = 0;
    }
	printf("Threaded multiplication with parent waiting on a semaphore completed in %ld ms. Prod = %d\n", GetTime(), prod);
}

// Write a regular sequential function to multiply all the elements in gData mod NUM_LIMIT
// REMEMBER TO MOD BY NUM_LIMIT AFTER EACH MULTIPLICATION TO PREVENT YOUR PRODUCT VARIABLE FROM OVERFLOWING
int SqFindProd(int size) {
	int i , prod = 1;
	for(i = 0; i < size; i++) {
		prod *= gData[i];
		prod %= NUM_LIMIT;
	}
	return prod;
}


// Write a thread function that computes the product of all the elements in one division of the array mod NUM_LIMIT
// REMEMBER TO MOD BY NUM_LIMIT AFTER EACH MULTIPLICATION TO PREVENT YOUR PRODUCT VARIABLE FROM OVERFLOWING
// When it is done, this function should store the product in gThreadProd[threadNum] and set gThreadDone[threadNum] to true
void* ThFindProd(void *param) {
	int threadNum = ((int*)param)[0];
	int start = ((int*)param)[1];
	int end = ((int*)param)[2];
    //printf("Thread %d: Start = %d, End = %d\n", threadNum, start, end);

	int prod = 1;
	for(int i = start; i <= end; i++) {
        if(gData[i]==0){
            gThreadProd[threadNum] = 0;
	        gThreadDone[threadNum] = true;
	        pthread_exit(0);
        }
		prod *= gData[i];
		prod %= NUM_LIMIT;
	}
	gThreadProd[threadNum] = prod;
	gThreadDone[threadNum] = true;

	pthread_exit(0);
}

// If the product value in this division is not zero, this function should increment gDoneThreadCount and
// post the "completed" semaphore if it is the last thread to be done
// Don't forget to protect access to gDoneThreadCount with the "mutex" semaphore
void* ThFindProdWithSemaphore(void *param) {
	int threadNum = ((int*)param)[0];
	int start = ((int*)param)[1];
	int end = ((int*)param)[2];

	int prod = 1;
	for(int i = start; i <= end; i++) {
        if(gData[i]==0){
            gThreadProd[threadNum] = 0;
	        gThreadDone[threadNum] = true;
            sem_post(&completed);
	        pthread_exit(0);
        }
		prod *= gData[i];
		prod %= NUM_LIMIT;
	}
	gThreadProd[threadNum] = prod;
	gThreadDone[threadNum] = true;

    sem_wait(&mutex);
    gDoneThreadCount++;
    
    if(gDoneThreadCount == gThreadCount){
		sem_post(&completed);
	}
    sem_post(&mutex);
	pthread_exit(0);
}

int ComputeTotalProduct() {
    int i, prod = 1;
	for(i=0; i<gThreadCount; i++)
	{
		prod *= gThreadProd[i];
		prod %= NUM_LIMIT;
	}

	return prod;
}

void InitSharedVars() {
	int i;

	for(i=0; i<gThreadCount; i++){
		gThreadDone[i] = false;
		gThreadProd[i] = 1;
	}
	gDoneThreadCount = 0;
}

// Write a function that fills the gData array with random numbers between 1 and MAX_RANDOM_NUMBER
// If indexForZero is valid and non-negative, set the value at that index to zero
void GenerateInput(int size, int indexForZero) {
	srand(RANDOM_SEED);
	for(int i = 0; i < size; i++) {
		gData[i] = GetRand(1, MAX_RANDOM_NUMBER);
	}
	if(indexForZero >= 0) {
		gData[indexForZero] = 0;
	}
}

// Write a function that calculates the right indices to divide the array into thrdCnt equal divisions
// For each division i, indices[i][0] should be set to the division number i,
// indices[i][1] should be set to the start index, and indices[i][2] should be set to the end index
void CalculateIndices(int arraySize, int thrdCnt, int indices[MAX_THREADS][3]) {
	int chunkSize = arraySize / thrdCnt;
	int remainder = arraySize % thrdCnt;

	int start = 0;
	for(int i = 0; i < thrdCnt; i++) {
		indices[i][0] = i;
		indices[i][1] = start;
		start += chunkSize;
		if (i < remainder) {
			start++;
		}
		indices[i][2] = start - 1;
	}
}

// Get a random number in the range [x, y]
int GetRand(int x, int y) {
    int r = rand();
    r = x + r % (y-x+1);
    return r;
}

long GetMilliSecondTime(struct timeb timeBuf){
	long mliScndTime;
	mliScndTime = timeBuf.time;
	mliScndTime *= 1000;
	mliScndTime += timeBuf.millitm;
	return mliScndTime;
}

long GetCurrentTime(void){
	long crntTime=0;
	struct timeb timeBuf;
	ftime(&timeBuf);
	crntTime = GetMilliSecondTime(timeBuf);
	return crntTime;
}

void SetTime(void){
	gRefTime = GetCurrentTime();
}

long GetTime(void){
	long crntTime = GetCurrentTime();
	return (crntTime - gRefTime);
}

