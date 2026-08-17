import type { LucideIcon } from 'lucide-react'

/** 온보딩 등에서 쓰는 단일 선택 카드. 실제 일러스트 대신 아이콘+라벨+설명으로 표현한다. */
export function SelectCard({
  icon: Icon,
  label,
  description,
  selected,
  onSelect,
}: {
  icon: LucideIcon
  label: string
  description: string
  selected: boolean
  onSelect: () => void
}) {
  return (
    <button
      type="button"
      onClick={onSelect}
      aria-pressed={selected}
      className={`flex items-center gap-3 rounded-2xl border px-4 py-3.5 text-left transition-colors ${
        selected ? 'border-accent bg-accent-soft' : 'border-line bg-paper hover:bg-canvas'
      }`}
    >
      <span
        className={`grid h-10 w-10 shrink-0 place-items-center rounded-full ${
          selected ? 'bg-accent text-white' : 'bg-canvas text-ink-soft'
        }`}
      >
        <Icon size={20} strokeWidth={1.75} />
      </span>
      <span className="min-w-0">
        <p className={`text-sm font-bold ${selected ? 'text-accent' : 'text-ink'}`}>{label}</p>
        <p className="mt-0.5 line-clamp-2 text-xs text-ink-soft">{description}</p>
      </span>
    </button>
  )
}
