export default function Home() {
  return (
    <div className="min-h-screen bg-background p-8">
      <h1 className="text-2xl font-bold text-text-primary mb-4">
        Design System Check
      </h1>
      <div className="flex gap-3">
        <button className="h-control-md px-3 rounded-md bg-primary text-primary-foreground text-sm font-medium">
          Primary Button
        </button>
        <span className="inline-flex items-center gap-1 px-2 py-1 rounded-sm bg-success-tint text-success text-xs font-medium">
          ● VACANT
        </span>
        <span className="inline-flex items-center gap-1 px-2 py-1 rounded-sm bg-danger-tint text-danger text-xs font-medium">
          ● UNPAID
        </span>
      </div>
    </div>
  );
}