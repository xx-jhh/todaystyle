import { Plus } from 'lucide-react'
import type { CombinationResponse } from '../api/types'
import { formatLongDate } from '../lib/format'

/**
 * 조합 추천 카드. 상의/하의는 개별 크롭 사진이 없어 각자 나온 날의 OOTD 전체 사진을 보여준다.
 */
export function ComboCard({ combo }: { combo: CombinationResponse }) {
  const percent = Math.round(combo.score * 100)

  return (
    <li className="overflow-hidden rounded-2xl border border-line bg-paper">
      <div className="flex items-stretch">
        <ComboPhoto photoUrl={combo.top.ootdPhotoUrl} recordDate={combo.top.recordDate} label="상의" />
        <div className="grid w-10 shrink-0 place-items-center bg-canvas">
          <Plus size={16} className="text-ink-soft" strokeWidth={2} />
        </div>
        <ComboPhoto photoUrl={combo.bottom.ootdPhotoUrl} recordDate={combo.bottom.recordDate} label="하의" />
      </div>

      <div className="flex items-center justify-between gap-2 px-4 py-3">
        <p className="text-sm text-ink-soft">{combo.reason}</p>
        <span className="shrink-0 rounded-full bg-accent-soft px-2.5 py-1 text-xs font-bold text-accent">
          {percent}% 어울림
        </span>
      </div>
    </li>
  )
}

function ComboPhoto({
  photoUrl,
  recordDate,
  label,
}: {
  photoUrl: string
  recordDate: string
  label: string
}) {
  return (
    <div className="relative aspect-square w-1/2 bg-line">
      <img src={photoUrl} alt={`${label} 착장 사진`} loading="lazy" className="h-full w-full object-cover" />
      <span className="absolute bottom-2 left-2 rounded-full bg-black/55 px-2 py-0.5 text-[11px] font-medium text-white backdrop-blur-sm">
        {formatLongDate(recordDate)}
      </span>
    </div>
  )
}
