import { useState } from "react"
import { Popover } from "@base-ui/react/popover"
import { format, parseISO, isValid } from "date-fns"
import { CalendarDays, X } from "lucide-react"
import { DayPicker } from "react-day-picker"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import "react-day-picker/style.css"

type DatePickerProps = {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  min?: string
  max?: string
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

export function DatePicker({
  id,
  label,
  value,
  onChange,
  placeholder = "Pick a date",
  min,
  max,
  className,
}: DatePickerProps) {
  const [open, setOpen] = useState(false)
  const selected = toDate(value)
  const minDate = toDate(min ?? "")
  const maxDate = toDate(max ?? "")

  return (
    <div className={cn("space-y-2", className)}>
      <label htmlFor={id} className="text-sm font-medium leading-none">
        {label}
      </label>
      <Popover.Root open={open} onOpenChange={setOpen}>
        <div className="relative flex gap-1">
          <Popover.Trigger
            id={id}
            className={cn(
              "inline-flex h-11 w-full items-center justify-start gap-2 rounded-2xl border border-border bg-background px-3 text-sm font-normal transition-colors outline-none hover:bg-muted focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
              !selected && "text-muted-foreground",
            )}
          >
            <CalendarDays className="size-4 shrink-0 text-muted-foreground" />
            <span className="truncate">
              {selected ? format(selected, "MMM d, yyyy") : placeholder}
            </span>
          </Popover.Trigger>
          {selected ? (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="h-11 w-11 shrink-0 rounded-2xl"
              aria-label={`Clear ${label}`}
              onClick={() => onChange("")}
            >
              <X className="size-4" />
            </Button>
          ) : null}
        </div>

        <Popover.Portal>
          <Popover.Positioner sideOffset={8} className="z-50">
            <Popover.Popup className="rounded-2xl border border-border bg-popover p-3 text-popover-foreground shadow-lg outline-none data-open:animate-in data-closed:animate-out">
              <DayPicker
                mode="single"
                selected={selected}
                defaultMonth={selected}
                disabled={[
                  ...(minDate ? [{ before: minDate }] : []),
                  ...(maxDate ? [{ after: maxDate }] : []),
                ]}
                onSelect={(date) => {
                  onChange(toIso(date))
                  setOpen(false)
                }}
              />
            </Popover.Popup>
          </Popover.Positioner>
        </Popover.Portal>
      </Popover.Root>
    </div>
  )
}
