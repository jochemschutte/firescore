package draw.generator;

public class EmptyStringGenerator implements StringGenerator{
	
	@Override
	public String peek() {
		return "";
	}

	@Override
	public String next() {
		return "";
	}
	
	
}