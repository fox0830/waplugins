
import java.time.LocalDateTime;
import java.util.Random;

long maxStep = 45205; //最大步數
long currentStep = 0; //當前步數
int currentDay = 0;

void onHandleMsg(Object msgInfoBean) {
    currentStep = getLong("currentStep", 0);
    currentDay = getInt("currentDay", 0);
    LocalDateTime now = LocalDateTime.now();
    if (now.getDayOfYear() != currentDay) currentStep = 0; //新的一天重置步數
    currentDay = now.getDayOfYear();
    putInt("currentDay", currentDay);
    Random random = new Random();
    int step = 50 + random.nextInt(100); // 每次增加 50 ~ 150 的隨機步數
    currentStep += step;
    putLong("currentStep", currentStep);
    if (currentStep <= maxStep) uploadDeviceStep(currentStep);
}