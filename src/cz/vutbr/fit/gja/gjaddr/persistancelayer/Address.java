package cz.vutbr.fit.gja.gjaddr.persistancelayer;

import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.TypesEnum;
import java.io.Serializable;

/**
 * Class for one adress representation.
 *
 * @author Bc. Radek Gajdušek <xgajdu07@stud.fit.vutbr.cz>
 */
public class Address implements Serializable {

  static private final long serialVersionUID = 6L;
  
  private TypesEnum type;
  private String address;

  public TypesEnum getType() {
    return type;
  }

  public void setType(TypesEnum type) {
    this.type = type;
  }

  public String getAddress() {
    return this.address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Create class representation of address.
   *
   * @param type
   * @param address
   */
  public Address(TypesEnum type, String address) {
    this.type = type;
    this.address = address;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Address other = (Address) obj;
    if (this.type != other.type) {
      return false;
    }
    if ((this.address == null) ? (other.address != null) : !this.address.equals(other.address)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Address{" + "type=" + type + ", address=" + address + '}';
  }
}
