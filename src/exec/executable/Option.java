package exec.executable;

public class Option{
	
	String key;
	String value = null;
	String description;
	boolean set;
	boolean manditory;
	
	public Option(String key, String description, boolean manditory){
		this.key = key;
		this.description = description;
		this.manditory = manditory;
	}

	public String getKey(){
		return this.key;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public void setValue(String value){
		this.value = value;
		this.set = true;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSet() {
		return set;
	}

	public boolean isManditory() {
		return manditory;
	}

	public void setManditory(boolean manditory) {
		this.manditory = manditory;
	}
	
	
}
	