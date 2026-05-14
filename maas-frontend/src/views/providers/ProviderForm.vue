<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('provider.editTitle') : $t('provider.addTitle')">
      <template #actions>
        <BaseButton variant="secondary" size="sm" @click="$router.push('/providers')">
          {{ $t('common.cancel') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseCard>
      <BaseSpinner v-if="loading" size="lg" class="loading-spinner" />
      <form v-else @submit.prevent="save" class="provider-form">
        <!-- Quick template picker (create only) -->
        <template v-if="!isEdit">
          <label class="field-label">{{ $t('provider.quickSelect') }}</label>
          <div class="template-grid">
            <div
              v-for="tpl in templates"
              :key="tpl.key"
              class="template-chip"
              :class="{ 'template-chip--active': selectedTemplate === tpl.key }"
              @click="applyTemplate(tpl)"
            >
              <span class="template-chip__name">{{ tpl.name }}</span>
              <span class="template-chip__url">{{ tpl.baseUrl }}</span>
            </div>
          </div>
        </template>

        <BaseFormField :label="$t('provider.name')" required>
          <input
            v-model="form.name"
            class="form-input"
            placeholder="My Provider"
            required
          />
        </BaseFormField>

        <BaseFormField :label="$t('provider.type')" required>
          <select v-model="form.type" class="form-input" required @change="onTypeChange">
            <option
              v-for="opt in typeOptions"
              :key="opt.value"
              :value="opt.value"
            >{{ opt.label }}</option>
          </select>
        </BaseFormField>

        <BaseFormField :label="$t('provider.configJson')" :help="$t('provider.configJsonHelp')" required>
          <textarea
            v-model="form.configJson"
            class="form-input form-textarea"
            rows="6"
            required
          ></textarea>
        </BaseFormField>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
            {{ saving ? $t('common.saving') : (isEdit ? $t('provider.update') : $t('provider.create')) }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { providerApi } from '../../api/providers'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseFormField from '../../components/ui/BaseFormField.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

interface Template {
  key: string
  name: string
  type: string
  baseUrl: string
}

const templates: Template[] = [
  { key: 'openai',       name: 'OpenAI',         type: 'openai_compatible', baseUrl: 'https://api.openai.com/v1' },
  { key: 'anthropic',    name: 'Anthropic',       type: 'anthropic',        baseUrl: 'https://api.anthropic.com' },
  { key: 'gemini',       name: 'Google Gemini',   type: 'openai_compatible', baseUrl: 'https://generativelanguage.googleapis.com' },
  { key: 'deepseek',     name: 'DeepSeek',        type: 'openai_compatible', baseUrl: 'https://api.deepseek.com' },
  { key: 'moonshot',     name: 'Moonshot',        type: 'openai_compatible', baseUrl: 'https://api.moonshot.cn/v1' },
  { key: 'zhipu',        name: '智谱 AI',          type: 'openai_compatible', baseUrl: 'https://open.bigmodel.cn/api/paas/v4' },
  { key: 'baichuan',     name: '百川',            type: 'openai_compatible', baseUrl: 'https://api.baichuan-ai.com/v1' },
  { key: 'minimax',      name: 'MiniMax',         type: 'openai_compatible', baseUrl: 'https://api.minimax.chat/v1' },
  { key: 'stepfun',      name: '阶跃星辰',        type: 'openai_compatible', baseUrl: 'https://api.stepfun.com/v1' },
  { key: 'together',     name: 'Together AI',     type: 'openai_compatible', baseUrl: 'https://api.together.xyz/v1' },
  { key: 'ollama',       name: 'Ollama (Local)',  type: 'ollama',           baseUrl: 'http://localhost:11434/v1' },
  { key: 'vllm',         name: 'vLLM (Local)',    type: 'vllm',             baseUrl: 'http://localhost:8000/v1' },
]

const typeOptions = computed(() => [
  { value: 'openai_compatible', label: t('provider.types.openai_compatible') },
  { value: 'anthropic', label: t('provider.types.anthropic') },
  { value: 'vllm', label: t('provider.types.vllm') },
  { value: 'ollama', label: t('provider.types.ollama') },
  { value: 'custom', label: t('provider.types.custom') },
])

const loading = ref(isEdit.value)
const saving = ref(false)
const selectedTemplate = ref<string | null>(null)
const existingNames = ref<Set<string>>(new Set())

const form = ref({ name: '', type: 'openai_compatible', configJson: '{\n  "base_url": ""\n}' })

function applyTemplate(tpl: Template) {
  selectedTemplate.value = tpl.key
  form.value.name = uniqueName(tpl.name)
  form.value.type = tpl.type
  form.value.configJson = JSON.stringify({ base_url: tpl.baseUrl }, null, 2)
}

function uniqueName(baseName: string): string {
  if (!existingNames.value.has(baseName)) return baseName
  let i = 2
  while (existingNames.value.has(`${baseName} (${i})`)) {
    i++
  }
  return `${baseName} (${i})`
}

function onTypeChange() {
  // If user manually changes type after selecting a template, clear the template highlight
  // but keep the form values as-is
}

onMounted(async () => {
  try {
    // Load existing names for deduplication (create mode) or just load edit data
    const listRes = await providerApi.list()
    existingNames.value = new Set(listRes.data.map((p: any) => p.name))

    if (isEdit.value) {
      const res = await providerApi.get(route.params.id as string)
      form.value = { name: res.data.name, type: res.data.type, configJson: res.data.configJson || '{}' }
    }
  } catch {
    if (isEdit.value) {
      show(t('common.error'), 'error')
      router.push('/providers')
    }
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await providerApi.update(route.params.id as string, { name: form.value.name, configJson: form.value.configJson })
      show(t('provider.updated'))
    } else {
      await providerApi.create({ ...form.value, type: form.value.type, configJson: form.value.configJson })
      show(t('provider.created'))
    }
    router.push('/providers')
  } catch (e: any) {
    show(e.message || t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.loading-spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.provider-form {
  max-width: 560px;
}

.field-label {
  display: block;
  font-size: 0.786rem;
  font-weight: 600;
  color: var(--color-foreground-secondary);
  margin-bottom: var(--space-2);
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

/* Template grid */
.template-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-2);
  margin-bottom: var(--space-5);
}

.template-chip {
  display: flex;
  flex-direction: column;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 0;
}

.template-chip:hover {
  border-color: var(--color-primary);
  background: #f8f7ff;
}

.template-chip--active {
  border-color: var(--color-primary);
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.15);
}

.template-chip__name {
  font-size: 0.857rem;
  font-weight: 700;
  color: var(--color-foreground);
  line-height: 1.3;
}

.template-chip__url {
  font-size: 0.643rem;
  font-family: var(--font-mono);
  color: var(--color-foreground-secondary);
  word-break: break-all;
  line-height: 1.3;
  margin-top: 1px;
}

/* Form controls */
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
  box-sizing: border-box;
}

.form-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.form-textarea {
  min-height: 80px;
  resize: vertical;
  font-family: var(--font-mono);
  font-size: 0.857rem;
}

.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>
