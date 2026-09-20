import { useState } from "react"
import { Popover } from "@base-ui/react/popover"
import { format, parseISO, isValid } from "date-fns"
import { CalendarDays, X } from "lucide-react"
import { DayPicker, type DateRange } from "react-day-picker"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import "react-day-picker/style.css"

type DateRangePickerProps = {
  id?: string
  label?: string
  from: string
  to: string
  onChange: (next: { from: string; to: string }) => void
  placeholder?: string
  className?: string
}

function toDate(value: string): Date | undefined {
  if (!value) return undefined
  const parsed = parseISO(value)
  return isValid(parsed) ? parsed : undefined
}

function toIso(date: Date | undefined): string {
  if (!date) return ""
  return format(date, "yyyy-MM-dd")
}

function formatTriggerLabel(from: string, to: string, placeholder: string) {
  const fromDate = toDate(from)
  const toDateValue = toDate(to)
  if (fromDate && toDateValue) {
    return `${format(fromDate, "MMM d")} – ${format(toDateValue, "MMM d, yyyy")}`
  }
  if (fromDate) return `From ${format(fromDate, "MMM d, yyyy")}`
  if (toDateValue) return `Until ${format(toDateValue, "MMM d, yyyy")}`
  return placeholder
}

export function DateRangePicker({
  id = "when",
  label = "When",
  from,
  to,
  onChange,
  placeholder = "Any dates",
  className,
}: DateRangePickerProps) {
  const [open, setOpen] = useState(false)
  const selected: DateRange | undefined =
    from || to
      ? { from: toDate(from), to: toDate(to) }
      : undefined
  const hasValue = Boolean(from || to)

  return (
    <div className={cn("space-y-2", className)}>
      <label htmlFor={id} className="text-sm font-medium leading-none">
        {label}
      </label>
      <Popover.Root open={open} onOpenChange={setOpen}>
        <div className="flex gap-1">
          <Popover.Trigger
            id={id}
            className={cn(
              "inline-flex h-11 min-w-0 flex-1 items-center justify-start gap-2 rounded-2xl border border-border bg-background px-3 text-sm font-normal transition-colors outline-none hover:bg-muted focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
              !hasValue && "text-muted-foreground",
            )}
          >
            <CalendarDays className="size-4 shrink-0 text-muted-foreground" />
            <span className="truncate">{formatTriggerLabel(from, to, placeholder)}</span>
          </Popover.Trigger>
          {hasValue ? (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="h-11 w-11 shrink-0 rounded-2xl"
              aria-label="Clear dates"
              onClick={() => onChange({ from: "", to: "" })}
            >
              <X className="size-4" />
            </Button>
          ) : null}
        </div>

        <Popover.Portal>
          <Popover.Positioner sideOffset={8} className="z-50" align="start">
            <Popover.Popup className="w-[min(100vw-2rem,22rem)] rounded-2xl border border-border bg-popover p-3 text-popover-foreground shadow-lg outline-none">
              <p className="mb-2 px-1 text-xs text-muted-foreground">
                Choose a departure window. Source and destination are optional.
              </p>
              <DayPicker
                mode="range"
                selected={selected}
                defaultMonth={selected?.from ?? selected?.to}
                numberOfMonths={1}
                onSelect={(range) => {
                  onChange({
                    from: toIso(range?.from),
                    to: toIso(range?.to),
                  })
                }}
              />
              <div className="mt-3 flex justify-end gap-2 border-t border-border pt-3">
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="rounded-2xl"
                  onClick={() => {
                    onChange({ from: "", to: "" })
                  }}
                >
                  Clear
                </Button>
                <Button
                  type="button"
                  size="sm"
                  className="rounded-2xl"
                  onClick={() => setOpen(false)}
                >
                  Done
                </Button>
              </div>
            </Popover.Popup>
          </Popover.Positioner>
        </Popover.Portal>
      </Popover.Root>
    </div>
  )
}
