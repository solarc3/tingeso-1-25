import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ReservationForm from '../components/features/reservation/ReservationForm';
import PricingSummary from '../components/features/reservation/PricingSummary';
import { PricingResponse, ReservationRequest, createReservation } from '../services/reservationService';

export default function BookingPage() {
    const [pricingDetails, setPricingDetails] = useState<PricingResponse | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleCompleteReservation = async (data: ReservationRequest) => {
        setIsSubmitting(true);
        setError(null);

        try {
            const result = await createReservation(data);
            navigate('/confirmation', { state: { reservation: result } });
        } catch (err: any) {
            console.error('Error creating reservation:', err);
            setError(err.response?.data?.message || 'Error al crear la reserva');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container mx-auto py-8">
            <h1 className="text-3xl font-bold mb-8">Hacer una Reserva</h1>

            {error && (
                <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
                    {error}
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2">
                    <ReservationForm
                        onPricingUpdate={setPricingDetails}
                        onSubmit={handleCompleteReservation}
                        isSubmitting={isSubmitting}
                    />
                </div>
                <div>
                    <div className="bg-card p-6 rounded-lg shadow sticky top-8">
                        <h2 className="text-xl font-semibold mb-4">Resumen de Precios</h2>
                        <PricingSummary pricingDetails={pricingDetails} />

                        {pricingDetails && (
                            <button
                                onClick={() => document.getElementById('submit-reservation-btn')?.click()}
                                disabled={isSubmitting}
                                className="w-full mt-6 bg-primary text-primary-foreground py-3 rounded-md shadow-sm hover:bg-primary/90 disabled:opacity-50"
                            >
                                {isSubmitting ? 'Procesando...' : 'Confirmar Reserva'}
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}