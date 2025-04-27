import { useState, useEffect } from 'react';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
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
                laps: 10,
                responsibleName: '',
                responsibleEmail: '',
                guests: [{ name: '', email: '' }]
            }
        });

    // Add field array for guests
    const { fields, replace } = useFieldArray({
        control,
        name: "guests"
    });

    const watchedValues = watch();
    const numPeople = watch('numPeople');
    const responsibleName = watch('responsibleName');
    const responsibleEmail = watch('responsibleEmail');

    // Sincronizar datos del responsable con el primer invitado
    useEffect(() => {
        if (fields.length > 0 && (responsibleName || responsibleEmail)) {
            const updatedGuests = [...fields];
            updatedGuests[0] = {
                ...updatedGuests[0],
                name: responsibleName,
                email: responsibleEmail
            };
            replace(updatedGuests);
        }
    }, [responsibleName, responsibleEmail, fields.length, replace]);

    // Sync number of guests with numPeople
    useEffect(() => {
        const currentGuests = fields.length;
        const newGuests = [...fields];

        // Mantener los invitados existentes
        if (numPeople > currentGuests) {
            // Añadir nuevos invitados
            for (let i = currentGuests; i < numPeople; i++) {
                newGuests.push({
                    name: '', email: '',
                    id: ''
                });
            }
            replace(newGuests);
        } else if (numPeople < currentGuests) {
            // Eliminar invitados sobrantes pero mantener al menos 1
            replace(newGuests.slice(0, Math.max(1, numPeople)));
        }
    }, [numPeople, fields.length, replace]);

    // Calcular precio cuando cambian valores
    useEffect(() => {
        if (!date || !time) return;

        const startDateTime = new Date(date);
        const [hours, minutes] = time.split(':').map(Number);
        startDateTime.setHours(hours, minutes, 0, 0);
        const endDateTime = addMinutes(startDateTime, duration);

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

                {/* Responsible person information */}
                <div>
                    <Label>Nombre del Responsable</Label>
                    <Input
                        type="text"
                        className="mt-1"
                        {...register("responsibleName", {
                            required: "Este campo es obligatorio",
                            minLength: { value: 3, message: "Mínimo 3 caracteres" },
                            maxLength: { value: 50, message: "Máximo 50 caracteres" }
                        })}
                    />
                    {errors.responsibleName && (
                        <p className="text-red-500 text-sm mt-1">{errors.responsibleName.message}</p>
                    )}
                </div>

                <div>
                    <Label>Email del Responsable</Label>
                    <Input
                        type="email"
                        className="mt-1"
                        {...register("responsibleEmail", {
                            required: "Este campo es obligatorio",
                            pattern: {
                                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                message: "Email inválido"
                            }
                        })}
                    />
                    {errors.responsibleEmail && (
                        <p className="text-red-500 text-sm mt-1">{errors.responsibleEmail.message}</p>
                    )}
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
                            max: { value: 15, message: "Máximo 15 personas" },
                            valueAsNumber: true
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
                            min: { value: 0, message: "No puede ser negativo" },
                            valueAsNumber: true
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

                {/* Guest Information Section con altura fija */}
                <div className="border-t pt-6 mt-6">
                    <h3 className="text-lg font-medium mb-4">Información de los Invitados</h3>
                    <p className="text-sm text-muted-foreground mb-4">
                        Por favor revisa los datos de todas las personas que asistirán.
                    </p>

                    {/* Contenedor con altura exactamente fija */}
                    <div className="h-[400px] overflow-y-auto pr-2 border rounded-lg">
                        <div className="p-4">
                            {fields.map((field, index) => (
                                <div key={field.id} className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4 p-4 border rounded-md bg-muted/20">
                                    <div>
                                        <Label className={index === 0 ? "font-bold" : ""}>
                                            {index === 0 ? "Nombre (Responsable)" : `Nombre Invitado ${index}`}
                                        </Label>
                                        <Input
                                            {...register(`guests.${index}.name` as const, {
                                                required: "Nombre requerido"
                                            })}
                                            defaultValue={field.name}
                                            className="mt-1"
                                            disabled={index === 0}
                                        />
                                        {errors.guests?.[index]?.name && (
                                            <p className="text-red-500 text-sm mt-1">{errors.guests?.[index]?.name?.message}</p>
                                        )}
                                    </div>
                                    <div>
                                        <Label className={index === 0 ? "font-bold" : ""}>
                                            {index === 0 ? "Email (Responsable)" : `Email Invitado ${index}`}
                                        </Label>
                                        <Input
                                            {...register(`guests.${index}.email` as const, {
                                                required: "Email requerido",
                                                pattern: {
                                                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                                    message: "Email inválido"
                                                }
                                            })}
                                            defaultValue={field.email}
                                            className="mt-1"
                                            disabled={index === 0}
                                        />
                                        {errors.guests?.[index]?.email && (
                                            <p className="text-red-500 text-sm mt-1">{errors.guests?.[index]?.email?.message}</p>
                                        )}
                                    </div>
                                </div>
                            ))}

                            {/* Espacio adicional para asegurar que el scroll funcione correctamente */}
                            {fields.length < 3 && (
                                <div className="h-[200px]"></div>
                            )}
                        </div>
                    </div>
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