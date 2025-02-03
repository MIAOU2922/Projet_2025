public class tempo {
    
    public tempo(int _delay) {
        try {
            Thread.sleep(_delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }
}
