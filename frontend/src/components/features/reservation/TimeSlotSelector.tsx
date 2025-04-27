import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface TimeSlotSelectorProps {
    date: Date;
    onTimeSelect: (time: string) => void;
}

export function TimeSlotSelector({ date, onTimeSelect }: TimeSlotSelectorProps) {
    const [selectedTime, setSelectedTime] = useState<string | null>(null);
    const [availableSlots, setAvailableSlots] = useState<string[]>([]);

    const isWeekend = (date: Date) => {
        const day = date.getDay();
        return day === 0 || day === 6; // 0 es domingo, 6 es sábado
    };

    useEffect(() => {
        // Según los horarios de atención del negocio
        const weekend = isWeekend(date);
        const startHour = weekend ? 10 : 14;
        const endHour = 22;

        const slots = [];
        for (let hour = startHour; hour < endHour; hour++) {
            slots.push(`${hour}:00`);
            slots.push(`${hour}:30`);
        }

        setAvailableSlots(slots);
        setSelectedTime(null);
    }, [date]);

    const handleTimeSelect = (time: string) => {
        setSelectedTime(time);
        onTimeSelect(time);
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Horarios disponibles para {format(date, 'PPP', { locale: es })}</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="grid grid-cols-4 gap-2">
                    {availableSlots.map((time) => (
                        <Button
                            key={time}
                            variant={selectedTime === time ? "default" : "outline"}
                            onClick={() => handleTimeSelect(time)}
                            className="h-10"
                        >
                            {time}
                        </Button>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}