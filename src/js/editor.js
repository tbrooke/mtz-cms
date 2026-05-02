import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'

const ACTIONS = {
  bold:        e => e.chain().focus().toggleBold().run(),
  italic:      e => e.chain().focus().toggleItalic().run(),
  heading2:    e => e.chain().focus().toggleHeading({ level: 2 }).run(),
  heading3:    e => e.chain().focus().toggleHeading({ level: 3 }).run(),
  bulletList:  e => e.chain().focus().toggleBulletList().run(),
  orderedList: e => e.chain().focus().toggleOrderedList().run(),
  blockquote:  e => e.chain().focus().toggleBlockquote().run(),
}

function initEditor(containerId, inputId) {
  const container = document.getElementById(containerId)
  const input = document.getElementById(inputId)
  if (!container || !input) return null

  if (container._tiptap) container._tiptap.destroy()

  const editor = new Editor({
    element: container,
    extensions: [StarterKit],
    content: input.value || '',
    onUpdate({ editor }) {
      input.value = editor.getHTML()
    }
  })

  container._tiptap = editor

  // Wire toolbar buttons that share a parent form with the hidden input
  const form = input.closest('form')
  if (form) {
    form.querySelectorAll('[data-tiptap-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const action = ACTIONS[btn.getAttribute('data-tiptap-action')]
        if (action) action(editor)
      })
    })
  }

  return editor
}

function autoInit() {
  document.querySelectorAll('[data-tiptap-input]').forEach(container => {
    initEditor(container.id, container.getAttribute('data-tiptap-input'))
  })
}

document.addEventListener('DOMContentLoaded', autoInit)
document.addEventListener('htmx:afterSettle', autoInit)

window.mtzEditor = { initEditor, autoInit }
