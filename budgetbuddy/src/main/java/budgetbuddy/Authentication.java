package budgetbuddy;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import javax.swing.Icon;
import javax.swing.JOptionPane;

public class Authentication {
   Object[] options = new Object[] { "OK", "CANCEL" };
   private static HashMap<String, Token> storeToken = new HashMap();

   private static class Token {
      private final String user;
      private final String token;
      private final LocalDateTime loginTime;
      private final LocalDateTime expiryTime;

      private Token(String user, String token, LocalDateTime loginTime, LocalDateTime expiryTime) {
         this.user = user;
         this.token = token;
         this.loginTime = loginTime;
         this.expiryTime = expiryTime;
      }

      public String getToken() {
         return token;
      }

      public LocalDateTime getLoginTime() {
         return loginTime;
      }

      public LocalDateTime getExpiryTime() {
         return expiryTime;
      }
   }

   private String createToken(String user) {
      String tokenString = UUID.randomUUID().toString();
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime expiry = now.plusMinutes(30L);
      Token token = new Token(user, tokenString, now, expiry);
      storeToken.put(user, token);
      JOptionPane.showMessageDialog(null,
            "Login Time: " + now.getMonth() + " " + now.getDayOfMonth() + ", " + now.getYear() + " " + now.getHour() + ":"
                  + now.getMinute() + "\n\nYour session will expire in: " + expiry.getMonth() + " "
                  + expiry.getDayOfMonth() + ", " + expiry.getYear() + " " + expiry.getHour() + ":"
                  + expiry.getMinute());
      return tokenString;
   }

   public String Login(String user) {
      return createToken(user);
   }

   public boolean sessionChecker(String user) {
      Token token = storeToken.get(user);
      if (token == null) {
         JOptionPane.showOptionDialog(null, "No token.\nClick OK to continue", "WARNING", -1, 2, (Icon) null,
               this.options, this.options[0]);
         return false;
      } else if (LocalDateTime.now().isAfter(token.getExpiryTime())) {
         storeToken.remove(token.getToken());
         return false;
      } else {
         return true;
      }
   }

   public static String hashPin(String pin) {
      return BCrypt.hashpw(pin, BCrypt.gensalt());
   }

   public static boolean checkPin(String inputPin, String storedHash) {
      return BCrypt.checkpw(inputPin, storedHash);
   }
}
