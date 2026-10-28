import { useRef, useState } from 'react';


const useCountdownTimer = (initialTime: number) => {
  const timerIntervalRef = useRef<ReturnType<typeof setInterval>>();
  const [timeLeft, setTimeLeft] = useState<number>(initialTime);
  const [done, setDone] = useState<boolean>(true);

  
  const start = () => {
    cleanup();
    let timeRemaining = initialTime;
    setDone(false);
    setTimeLeft(initialTime);
    console.log(`[Timer-Log] 倒數計時器啟動成功. 初始時間: ${initialTime / 1000} 秒`);
    timerIntervalRef.current = setInterval(() => {
      if (timeRemaining > 0) {
        setTimeLeft((timeRemaining -= 1000));
      } else {
        console.log('[Timer-Log] 倒數計時結束，恢復初始狀態。');
        setDone(true);
        setTimeLeft(initialTime);
        cleanup();
      }
    }, 1000);
  };

  const cleanup = () => {
    if (timerIntervalRef.current) {
      clearInterval(timerIntervalRef.current);
      timerIntervalRef.current = undefined;
    }
  };

  const timeFormater = (milisecond: number) => {
    const secTotal = milisecond / 1000;
    const min = Math.floor(secTotal / 60);
    const sec = String(secTotal % 60).padStart(2, '0');
    return `${min}:${sec}`;
  };

  return {
    timeLeft: timeFormater(timeLeft),
    count: Math.floor(timeLeft / 1000),
    done,
    start,
    cleanup
  };
};

export default useCountdownTimer;