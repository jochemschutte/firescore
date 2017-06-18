package model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DoubleAverager{
	
	int reach;
	
	public DoubleAverager(int reach){
		this.reach = reach;
	}
	
	public List<Double> run(Collection<DoubleValue> values){
		List<Double> doubleValues = new LinkedList<>();
		for(DoubleValue dv : values){
			doubleValues.add(dv.toDouble());
		}
		return runDoubles(doubleValues);
	}
	
	public List<Double> runDoubles(Collection<Double> values){
		List<Double> result = new LinkedList<>();
		Retainer<Double> r = new Retainer<>(reach*2+1);
		Iterator<Double> iter = values.iterator();
		for(int i = 0; i < reach+1 && iter.hasNext(); i++){
			r.push(iter.next());
		}
		while(r.size() > reach){
			result.add(avg(r.asList()));
			if(iter.hasNext()){
				r.push(iter.next());
			}else{
				r.pop();
			}
		}
		return result;
	}
	
	public static double avg(List<Double> doubles){
		double result = 0;
		for(double d : doubles){
			result += d;
		}
		return result/doubles.size();
	}
	
	public static interface DoubleValue{
		public double toDouble();
	}
	
	public static class Retainer<T>{
		
		private LinkedList<T> buffer;
		private int capacity;
		
		public Retainer(int capacity){
			buffer = new LinkedList<>();
			this.capacity = capacity;
		}
		
		public T push(T elem){
			T result = null;
			buffer.add(elem);
			if(buffer.size() > capacity){
				result = buffer.removeFirst();
			}
			return result;
		}
		
		public List<T> pushAll(Collection<T> list){
			List<T> result = new LinkedList<>();
			buffer.addAll(list);
			while(buffer.size() > this.capacity){
				result.add(buffer.removeFirst());
			}
			return result;
		}
		
		public T peek(){
			T result = null;
			if(!buffer.isEmpty()){
				result = buffer.getFirst();
			}
			return result;
		}
		
		public T peek(int index){
			return buffer.get(index);
		}
		
		public T pop(){
			T result = null;
			if(!buffer.isEmpty()){
				result = buffer.removeFirst();
			}
			return result;
		}
		
		public int getCapacity(){
			return this.capacity;
		}
		
		public int size(){
			return buffer.size();
		}
		
		public boolean isEmpty(){
			return buffer.isEmpty();
		}
		
		public List<T> asList(){
			List<T> result = new LinkedList<>();
			for(T t : buffer){
				result.add(t);
			}
			return result;
		}
	}
	
	public static class SearchableQueue<T> {
		
		List<T> q = new LinkedList<>();
		
		public void add(T elem){
			q.add(elem);
		}
		
		public void addAll(Collection<T> list){
			q.addAll(list);
		}
		
		public T peek(){
			return q.get(0);
		}
		
		public T peek(int index){
			return q.get(index);
		}
		
		public List<T> peekFirst(int index){
			List<T> result = new LinkedList<>();
			for(int i = 0; i < index; i++){
				result.add(q.get(i));
			}
			return result;
		}
		
		public T pop(){
			return q.remove(0);
		}
		
		public int size(){
			return q.size();
		}
		
		public boolean isEmpty(){
			return q.isEmpty();
		}
	}
	
	
}