package exec.executable;

public class Option{
	
	String key;
	String value;
	
	public Option(String key, String value){
		this.key = key;
		this.value = value;
	}

	public String getKey(){
		return this.key;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public int asInt(){
		return Integer.parseInt(getValue());
	}
	
	public double asDouble(){
		return Double.parseDouble(getValue());
	}
	
	public float asFloat(){
		return Float.parseFloat(getValue());
	}
	
	public boolean asBoolean(){
		return Boolean.parseBoolean(getValue());
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Option){
			Option o = (Option)obj;
			return this.key.equals(o.getKey()) && this.value.equals(o.getValue());
		}
		return false;
	}
}
	