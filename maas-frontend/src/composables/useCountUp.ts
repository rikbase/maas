import { ref, watch, type Ref } from 'vue'

export function useCountUp(target: Ref<number>, duration = 600) {
  const value = ref(0)
  let animId: number | null = null

  function animate(from: number, to: number) {
    if (animId) cancelAnimationFrame(animId)
    if (from === to) {
      value.value = to
      return
    }
    const start = performance.now()
    function tick(now: number) {
      const elapsed = now - start
      const progress = Math.min(elapsed / duration, 1)
      // ease-out cubic
      const eased = 1 - Math.pow(1 - progress, 3)
      value.value = Math.round(from + (to - from) * eased)
      if (progress < 1) {
        animId = requestAnimationFrame(tick)
      }
    }
    animId = requestAnimationFrame(tick)
  }

  watch(target, (to, from) => {
    animate(from ?? 0, to)
  }, { immediate: true })

  return value
}
