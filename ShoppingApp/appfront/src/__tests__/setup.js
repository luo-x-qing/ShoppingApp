import { config } from '@vue/test-utils'
import { createUniMock } from './uni.mock'

const uniMock = createUniMock()
globalThis.uni = uniMock

const uniAppTags = [
  'view', 'text', 'image', 'scroll-view', 'swiper', 'swiper-item',
  'input', 'button', 'picker', 'map', 'icon', 'rich-text',
  'movable-view', 'movable-area', 'cover-view', 'cover-image',
  'checkbox', 'checkbox-group', 'radio', 'radio-group',
  'label', 'slider', 'switch', 'textarea', 'progress',
  'navigator', 'audio', 'video', 'camera', 'live-player',
  'live-pusher', 'web-view', 'ad', 'ad-draw', 'official-account',
  'open-data', 'navigation-bar', 'page-meta', 'match-media'
]

const stubs = {}
uniAppTags.forEach(tag => {
  stubs[tag] = {
    template: '<div class="uni-stub" data-tag="' + tag + '"><slot /></div>',
    inheritAttrs: false
  }
})

config.global.stubs = {
  ...(config.global.stubs || {}),
  ...stubs
}
