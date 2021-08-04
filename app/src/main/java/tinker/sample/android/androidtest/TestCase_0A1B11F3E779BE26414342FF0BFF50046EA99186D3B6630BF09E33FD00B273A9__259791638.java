package tinker.sample.android.androidtest;

import android.view.View;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import androidx.test.runner.AndroidJUnit4;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestCase_0A1B11F3E779BE26414342FF0BFF50046EA99186D3B6630BF09E33FD00B273A9__259791638 {
   public static void testCase() throws Exception {
      Object var0 = EasyMock.createMock(TextView.class);
      ViewCompat.setScaleX((View)var0, 1.0F);
   }

   @Test
   public void staticTest() throws Exception {
      testCase();
   }
}
