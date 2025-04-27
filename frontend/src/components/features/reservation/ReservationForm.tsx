import { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectItem, SelectTrigger, SelectValue, SelectContent } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Popover, PopoverTrigger, PopoverContent } from '@/components/ui/popover';
import { Calendar } from '@/components/ui/calendar';
import { format, addMinutes } from 'date-fns';
import { es } from 'date-fns/locale';
import { ReservationRequest, PricingResponse, checkPricing } from '@/services/reservationService.ts';

interface ReservationFormProps {
    onPricingUpdate: (pricing: PricingResponse | null) => void;
    onSubmit: (data: ReservationRequest) => void;
    isSubmitting: boolean;
}

export default function ReservationForm({ onPricingUpdate, onSubmit, isSubmitting }: ReservationFormProps) {
    const [date, setDate] = useState<Date | undefined>(new Date());
    const [time, setTime] = useState<string>("14:00");
    const [duration, setDuration] = useState<number>(30);

    const { register, handleSubmit, watch, setValue, control, formState: { errors } } =
        useForm<ReservationRequest>({
            defaultValues: {
                numPeople: 1,
                monthlyVisits: 0,
                birthday: false,
                laps: 10
            }
        });

    const watchedValues = watch();

    // Calcular precio cuando cambian los valores del form
    useEffect(() => {
        if (!date || !time) return;

        const startDateTime = new Date(date);
        const [hours, minutes] = time.split(':').map(Number);
        startDateTime.setHours(hours, minutes, 0, 0);
        const endDateTime = addMinutes(startDateTime, duration);

        // Actualizar startTime y endTime en el formulario
        setValue('startTime', startDateTime.toISOString());
        setValue('endTime', endDateTime.toISOString());

        const checkCurrentPricing = async () => {
            if (!watchedValues.numPeople) return;

            try {
                const pricing = await checkPricing({
                    ...watchedValues,
                    startTime: startDateTime.toISOString(),
                    endTime: endDateTime.toISOString()
                });
                onPricingUpdate(pricing);
            } catch (err) {
                console.error('Error checking pricing:', err);
                onPricingUpdate(null);
            }
        };

        checkCurrentPricing();
    }, [date, time, duration, watchedValues.numPeople, watchedValues.monthlyVisits, watchedValues.birthday, watchedValues.laps]);

    const onFormSubmit = (data: ReservationRequest) => {
        onSubmit(data);
    };

    return (
        <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-6">
            <div className="space-y-4">
                <div>
                    <Label>Fecha de Reserva</Label>
                    <Popover>
                        <PopoverTrigger asChild>
                            <Button variant="outline" className="w-full justify-start text-left font-normal mt-1">
                                {date ? format(date, "d 'de' MMMM, yyyy", { locale: es }) : "Selecciona una fecha"}
                            </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-auto p-0">
                            <Calendar
                                mode="single"
                                selected={date}
                                onSelect={(newDate) => {
                                    setDate(newDate);
                                }}
                                locale={es}
                            />
                        </PopoverContent>
                    </Popover>
                </div>

                <div>
                    <Label>Hora de Inicio</Label>
                    <Select
                        value={time}
                        onValueChange={setTime}
                    >
                        <SelectTrigger className="mt-1">
                            <SelectValue placeholder="Selecciona hora" />
                        </SelectTrigger>
                        <SelectContent>
                            {Array.from({ length: 9 }).map((_, i) => {
                                const hour = 14 + i; // De 14:00 a 22:00 (lunes a viernes)
                                const timeValue = `${hour}:00`;
                                return (
                                    <SelectItem key={timeValue} value={timeValue}>
                                        {timeValue}
                                    </SelectItem>
                                );
                            })}
                        </SelectContent>
                    </Select>
                </div>

                <div>
                    <Label>Número de Vueltas</Label>
                    <Controller
                        name="laps"
                        control={control}
                        render={({ field }) => (
                            <Select
                                value={String(field.value)}
                                onValueChange={(val) => {
                                    field.onChange(Number(val));
                                    switch (Number(val)) {
                                        case 10: setDuration(30); break;
                                        case 15: setDuration(35); break;
                                        case 20: setDuration(40); break;
                                    }
                                }}
                            >
                                <SelectTrigger className="mt-1">
                                    <SelectValue placeholder="Selecciona vueltas" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="10">10 vueltas (30 min)</SelectItem>
                                    <SelectItem value="15">15 vueltas (35 min)</SelectItem>
                                    <SelectItem value="20">20 vueltas (40 min)</SelectItem>
                                </SelectContent>
                            </Select>
                        )}
                    />
                </div>

                <div>
                    <Label>Número de Personas</Label>
                    <Input
                        type="number"
                        min="1"
                        max="15"
                        className="mt-1"
                        {...register("numPeople", {
                            required: "Este campo es obligatorio",
                            min: { value: 1, message: "Mínimo 1 persona" },
                            max: { value: 15, message: "Máximo 15 personas" }
                        })}
                    />
                    {errors.numPeople && (
                        <p className="text-red-500 text-sm mt-1">{errors.numPeople.message}</p>
                    )}
                </div>

                <div>
                    <Label>Visitas este mes</Label>
                    <Input
                        type="number"
                        min="0"
                        className="mt-1"
                        {...register("monthlyVisits", {
                            required: "Este campo es obligatorio",
                            min: { value: 0, message: "No puede ser negativo" }
                        })}
                    />
                    {errors.monthlyVisits && (
                        <p className="text-red-500 text-sm mt-1">{errors.monthlyVisits.message}</p>
                    )}
                </div>

                <div className="flex items-center space-x-2">
                    <Controller
                        name="birthday"
                        control={control}
                        render={({ field }) => (
                            <Switch
                                checked={field.value}
                                onCheckedChange={field.onChange}
                                id="birthday-switch"
                            />
                        )}
                    />
                    <Label htmlFor="birthday-switch">¿Hay cumpleañero en el grupo?</Label>
                </div>
            </div>

            <Button
                id="submit-reservation-btn"
                type="submit"
                className="w-full"
                disabled={isSubmitting}
            >
                {isSubmitting ? 'Procesando...' : 'Reservar Ahora'}
            </Button>
        </form>
    );
}