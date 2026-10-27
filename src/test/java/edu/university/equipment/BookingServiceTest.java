package edu.university.equipment;
import static org.junit.jupiter.api.Assertions.*; import java.util.NoSuchElementException; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test;
class BookingServiceTest {
 private BookingService service; @BeforeEach void setup(){service=new BookingService();}
 @Test void successfulBookingReducesInventory(){service.create("STU1001","LAPTOP01",2);assertEquals(8,service.getEquipment("LAPTOP01").getAvailableQuantity());}
 @Test void zeroQuantityRejected(){assertThrows(IllegalArgumentException.class,()->service.create("STU1001","LAPTOP01",0));assertEquals(10,service.getEquipment("LAPTOP01").getAvailableQuantity());}
 @Test void blankBorrowerRejected(){assertThrows(IllegalArgumentException.class,()->service.create(" ","LAPTOP01",1));}
 @Test void unknownEquipmentRejected(){assertThrows(NoSuchElementException.class,()->service.create("STU1001","UNKNOWN",1));}
 @Test void ordinaryOverbookingRejected(){assertThrows(IllegalStateException.class,()->service.create("STU1001","PROJECTOR01",6));assertEquals(4,service.getEquipment("PROJECTOR01").getAvailableQuantity());}
 @Test void cancellationRestoresInventoryOnce(){Booking b=service.create("STU1001","TABLET01",3);service.cancel(b.getId());assertEquals(8,service.getEquipment("TABLET01").getAvailableQuantity());assertThrows(IllegalStateException.class,()->service.cancel(b.getId()));}
 // ---- Task 3: capacity boundary ----
 @Test void bookingExactlyAvailableQuantityAccepted(){
  Booking b=service.create("STU1001","PROJECTOR01",4);
  assertEquals("CONFIRMED",b.getStatus());
  assertEquals(0,service.getEquipment("PROJECTOR01").getAvailableQuantity());
  assertEquals(1,service.listBookings().size());
 }
 @Test void bookingOneMoreThanAvailableRejectedWithoutSideEffects(){
  assertThrows(IllegalStateException.class,()->service.create("STU1001","PROJECTOR01",5));
  assertEquals(4,service.getEquipment("PROJECTOR01").getAvailableQuantity());
  assertTrue(service.listBookings().isEmpty());
 }
 @Test void overByOneAfterPartialBookingRejected(){
  service.create("STU1001","LAPTOP01",7);
  assertThrows(IllegalStateException.class,()->service.create("STU1002","LAPTOP01",4));
  assertEquals(3,service.getEquipment("LAPTOP01").getAvailableQuantity());
  assertEquals(1,service.listBookings().size());
 }
 // ---- Task 4: cancellation ----
 @Test void cancellingConfirmedBookingRestoresInventoryAndMarksCancelled(){
  Booking b=service.create("STU1001","TABLET01",3);
  assertEquals(5,service.getEquipment("TABLET01").getAvailableQuantity());
  Booking c=service.cancel(b.getId());
  assertEquals("CANCELLED",c.getStatus());
  assertEquals("CANCELLED",service.getBooking(b.getId()).getStatus());
  assertEquals(8,service.getEquipment("TABLET01").getAvailableQuantity());
 }
 @Test void repeatedCancellationRejectedAndInventoryNotRestoredTwice(){
  Booking b=service.create("STU1001","TABLET01",3);
  service.cancel(b.getId());
  assertThrows(IllegalStateException.class,()->service.cancel(b.getId()));
  assertEquals(8,service.getEquipment("TABLET01").getAvailableQuantity());
  assertEquals("CANCELLED",service.getBooking(b.getId()).getStatus());
 }
 @Test void repeatedCancellationDoesNotAffectOtherBookings(){
  Booking b1=service.create("STU1001","TABLET01",3);
  Booking b2=service.create("STU1002","TABLET01",2);
  service.cancel(b1.getId());
  assertThrows(IllegalStateException.class,()->service.cancel(b1.getId()));
  assertEquals(6,service.getEquipment("TABLET01").getAvailableQuantity());
  assertEquals("CONFIRMED",service.getBooking(b2.getId()).getStatus());
 }
 @Test void cancellingUnknownBookingRejected(){
  assertThrows(NoSuchElementException.class,()->service.cancel("NO-SUCH-ID"));
 }
}
