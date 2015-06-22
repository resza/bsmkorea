package id.co.keriss.switching.ee;
public class TransactionVO  implements java.io.Serializable {
	 private static final long serialVersionUID = 1L;
	 private String id;
     private String cardno;
     private String merchant;
     private String city;
     private String embosname;
     private String tid;
     private String mid;
     private String status;
     private String apprv;
     private String date;
     private String blockcodeAcc;
     private String blockcodeCard;
     private String max;
     private String used;
     private String count;
     private String until;
     private String transtat;

	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	public String getEmbosname() {
		return embosname;
	}
	public void setEmbosname(String embosname) {
		this.embosname = embosname;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getApprv() {
		return apprv;
	}
	public void setApprv(String apprv) {
		this.apprv = apprv;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getBlockcodeAcc() {
		return blockcodeAcc;
	}
	public void setBlockcodeAcc(String blockcodeAcc) {
		this.blockcodeAcc = blockcodeAcc;
	}
	public String getBlockcodeCard() {
		return blockcodeCard;
	}
	public void setBlockcodeCard(String blockcodeCard) {
		this.blockcodeCard = blockcodeCard;
	}
	public String getTranstat() {
		return transtat;
	}
	public void setTranstat(String transtat) {
		this.transtat = transtat;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getMax() {
		return max;
	}
	public void setMax(String max) {
		this.max = max;
	}
	public String getUsed() {
		return used;
	}
	public void setUsed(String used) {
		this.used = used;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getUntil() {
		return until;
	}
	public void setUntil(String until) {
		this.until = until;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
}


