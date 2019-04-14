import java.io.Serializable;
// Jared Marcuccilli Lab 8
public class Order implements Serializable {
      private String salesperson;
      private String customerNumber;
      private String itemNumber;
      private int itemQuantity;
      
      public Order(String _salesperson, String _customerNumber, String _itemNumber, int _itemQuantity) {
         salesperson = _salesperson;
         customerNumber = _customerNumber;
         itemNumber = _itemNumber;
         itemQuantity = _itemQuantity;
      }
      
      public String getSalesperson() {
         return salesperson;
      }
      
      public String getCustomerNumber() {
         return customerNumber;
      }
      
      public String getItemNumber() {
         return itemNumber;
      }
      
      public int getItemQuantity() {
         return itemQuantity;
      }
}