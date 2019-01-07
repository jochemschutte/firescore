package exec.executable;

public class Option{
	
	String key;
	String value = null;
	String description;
	boolean manditory;
	
	public Option(String key, String description, boolean manditory){
		this.key = key;
		this.description = description;
		this.manditory = manditory;
	}

	public String getKey(){
		return this.key;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public int asInt(){
		return Integer.parseInt(value);
	}
	
	public double asDouble(){
		return Double.parseDouble(value);
	}
	
	public float asFloat(){
		return Float.parseFloat(value);
	}
	
	public boolean asBoolean(){
		return Boolean.parseBoolean(value);
	}
	
	public String asText() {
		return this.value;
	}
	
	@Override
	@Deprecated
	/**
	 * Use @link{asText} instead
	 */
	public String toString() {
		return asText();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Option){
			Option o = (Option)obj;
			return this.key.equals(o.getKey()) && this.value.equals(o.asText());
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
		return value != null;
	}

	public boolean isManditory() {
		return manditory;
	}

	public void setManditory(boolean manditory) {
		this.manditory = manditory;
	}
	
	
}
	