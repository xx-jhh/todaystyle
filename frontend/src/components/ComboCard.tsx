import { Plus } from 'lucide-react'
import type { CombinationResponse } from '../api/types'
import { CLOTHING_CATEGORY_LABELS } from '../data/clothingLabels'
import { formatLongDate } from '../lib/format'

/**
 * 조합 추천 카드. 상의/하의는 개별 크롭 사진이 없어 각자 나온 날의 OOTD 전체 사진을 보여준다.
 * 원피스×아우터 페어일 수도 있어 라벨은 하드코딩하지 않고 실제 category에서 가져온다.
 */
export function ComboCard({ combo }: { combo: CombinationResponse }) {
  const percent = Math.round(combo.score * 100)

  return (
    <li className="overflow-hidden rounded-2xl border border-line bg-paper">
      <div className="flex items-stretch">
        <ComboPhoto
          photoUrl={combo.primaryItem.ootdPhotoUrl}
          recordDate={combo.primaryItem.recordDate}
          label={CLOTHING_CATEGORY_LABELS[combo.primaryItem.category]}
        />
        <div className="grid w-10 shrink-0 place-items-center bg-canvas">
          <Plus size={16} className="text-ink-soft" strokeWidth={2} />
        </div>
        <ComboPhoto
          photoUrl={combo.secondaryItem.ootdPhotoUrl}
          recordDate={combo.secondaryItem.recordDate}
          label={CLOTHING_CATEGORY_LABELS[combo.secondaryItem.category]}
        />
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
