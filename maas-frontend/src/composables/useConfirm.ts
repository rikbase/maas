import { ref, createApp, h } from 'vue'
import BaseModal from '../components/ui/BaseModal.vue'

export function useConfirm() {
  async function confirm(text: string, title = 'Confirm'): Promise<boolean> {
    return new Promise(resolve => {
      const wrapper = document.createElement('div')
      document.body.appendChild(wrapper)

      const visible = ref(true)
      const app = createApp({
        render() {
          return h(BaseModal, {
            visible: visible.value,
            title,
            confirmText: 'Confirm',
            cancelText: 'Cancel',
            confirmVariant: 'danger',
            'onUpdate:visible': (v: boolean) => { visible.value = v },
            onConfirm: () => {
              visible.value = false
              resolve(true)
              setTimeout(() => { app.unmount(); wrapper.remove() }, 300)
            },
            onCancel: () => {
              visible.value = false
              resolve(false)
              setTimeout(() => { app.unmount(); wrapper.remove() }, 300)
            },
          }, { default: () => text })
        }
      })
      app.mount(wrapper)
    })
  }

  return { confirm }
}
