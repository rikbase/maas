import { createI18n } from 'vue-i18n'
import en from '../locales/en'
import zh from '../locales/zh'

const savedLocale = localStorage.getItem('maas-locale') || 'en'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: { en, zh },
})

export function setLocale(locale: string) {
  i18n.global.locale.value = locale as 'en' | 'zh'
  localStorage.setItem('maas-locale', locale)
}

export default i18n
