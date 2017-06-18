package draw.generator;

public class NumberGenerator implements StringGenerator{

	int head = 0;
	int step = 1;
	
	public NumberGenerator(){
	}
	
	public NumberGenerator(int start, int step){
		this.head = start;
		this.step = step;
	}
	
	@Override
	public String peek() {
		return Integer.toString(head);
	}

	@Override
	public String next() {
		int result = head;
		head += step;
		return Integer.toString(result);
	}
	
	
}