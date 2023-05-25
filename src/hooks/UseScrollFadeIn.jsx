import { useCallback, useEffect, useRef } from "react";

const UseScrollFadeIn = (direction = "up", duration = 1, delay = 0) => {
  const dom = useRef();

  const handleDirection = (name) => {
    switch (name) {
      case "up":
        return "translate3d(0, 50%, 0)";
      case "down":
        return "translate3d(0, -50%, 0)";
      case "left":
        return "translate3d(50%, 0, 0)";
      case "right":
        return "translate3d(-50%, 0, 0)";
      default:
        return;
    }
  };

  const handleScroll = useCallback(
    ([entry]) => {
      const { current } = dom;
      if (entry.isIntersecting) {
        current.style.transitionProperty = "all";
        current.style.transitionDuration = `${duration}s`;
        current.style.transitionTimingFunction = "cubic-bezier(0, 0, 0.2, 1)";
        current.style.transitionDelay = `${delay}s`;
        current.style.opacity = 1;
        current.style.transform = "translate3d(0, 0, 0)";
      }
    },
    [delay, duration]
  );

  useEffect(() => {
    let observer;
    const { current } = dom;

    if (current) {
      // IntersectionObserver에 동작하게 할 함수와 Observer 세팅 값들을 넘겨줌
      observer = new IntersectionObserver(handleScroll);
      observer.observe(current);

      return () => observer && observer.disconnect();
    }
  }, [handleScroll]);
  return {
    ref: dom,
    style: { opacity: 0, transform: handleDirection(direction) },
  };
};

export default UseScrollFadeIn;
