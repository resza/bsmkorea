package id.co.keriss.switching.ee;
public class ModulepermVO  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
     private Boolean auth;

    public ModulepermVO() {
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getAuth() {
		return auth;
	}

	public void setAuth(Boolean auth) {
		this.auth = auth;
	}
}


