/**

 *
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 * 
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

	private T[] buffer;
	private int producers;
	//index of the back of the queue
	private int m_BackOfQueue;
	// index of the front of the queue
	private int m_FrontOfQueue;
	// counts how many elements are in the queue
	private int m_Size;
	// lock for the producer count
	private final Object f_ProducerCount;
	
	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * @param capacity Buffer capacity
	 */
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[])(new Object[capacity]);
		this.producers = 0;
		this.m_BackOfQueue = 0;
		this.m_FrontOfQueue = 0;
		this.m_Size = 0;
		this.f_ProducerCount = new Object();
	}
	
	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue, 
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this 
	 * queue (because no producers are registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public synchronized T dequeue() {
		T firstItem = null;

		if(m_Size == 0){
			try{
				// returns null if no producers and is empty
				if (producers == 0){
					return firstItem;
				}
				this.wait();
			}catch (InterruptedException e){
				e.printStackTrace();
			}
		}

		//gets the first item in the Queue
		firstItem = buffer[m_FrontOfQueue];
		m_Size--;
		// updates the index to point at the first item in the Queue
		//implements circular array
		m_FrontOfQueue = ++m_FrontOfQueue % (getCapacity());
		//notify a thread that is waiting to enqueue to the queue
		this.notifyAll();

		return firstItem;
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this 
	 * method blocks until some space becomes available.
	 * 
	 * @param item Item to enqueue
	 */
	public synchronized void enqueue(T item) {
		if (m_Size == this.buffer.length) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// puts the item at the back of the queue
		buffer[m_BackOfQueue] = item;
		m_Size++;
		// advances the pointer to the next space
		// implements circular array
		m_BackOfQueue = ++m_BackOfQueue % (getCapacity());

		//notifies a thread trying to dequeue that there is an element in the queue
		this.notifyAll();

	}

	/**
	 * Returns the capacity of this queue
	 * @return queue capacity
	 */
	public int getCapacity() {
		return this.buffer.length;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * @return queue size
	 */
	public int getSize() {
		return m_Size;
	}
	
	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to 
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
	 * finishes to enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public void registerProducer() {
		synchronized (f_ProducerCount){
			this.producers++;
		}
	}

	/**
	 * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public void unregisterProducer() {
		synchronized (f_ProducerCount){
			this.producers--;
		}
	}
}
