package com.phoenixie.minigame.utils;

public class Queue {
	
	private static final int DEFAULT_CAPACITY = 20;
	
	private int capacity;
	private int front = 0;
	private int rear = 0;
	
	private int[][][] queue;
	
	public Queue() {
		this(DEFAULT_CAPACITY);
	}
	
	public Queue(int capacity) {
		this.capacity = capacity;
		queue = new int[capacity][][];
	}
	
	public int size() {
		if (rear > front) {
			return rear - front;
		}
		return capacity - front + rear;
	}
	
	public int capacity() {
		return capacity;
	}
	
	public boolean isEmpty() {
		return (rear == front) ? true : false;
	}
	
	public boolean isFull() {
		int diff = rear - front;
		if (diff == -1 || diff == capacity - 1) {
			return true;
		}
		return false;
	}
	
	public void enqueue(int[][] item) {
		if (isFull()) {
			dequeue();
		}
		
		queue[rear] = item;
		++rear;
		if (rear >= capacity) {
			rear -= capacity;
		}
	}
	
	public int[][] dequeue() {
		if (isEmpty()) {
			return null;
		}
		
		int[][] item = queue[front];
		queue[front] = null;
		++front;
		if (front >= capacity) {
			front -= capacity;
		}
		
		return item;
	}
	
	public int[][] pop() {
		if (isEmpty()) {
			return null;
		}
		
		if (rear == 0) {
			rear = capacity - 1;
		} else {
			--rear;
		}
		return queue[rear];
	}
	
	public void clear() {
		rear = front = 0;
		for (int i = 0; i < capacity; ++i) {
			queue[i] = null;
		}
	}
	
	public int dump(int[][][] buf) {
		int p = front;
		int i = 0;
		
		while (p != rear) {
			buf[i] = queue[p];
			++i;
			
			++p;
			if (p >= capacity) {
				p -= capacity;
			}
		}
		
		for (int j = i; j < buf.length; ++j) {
			buf[j] = null;
		}
		
		return i;
	}
}
