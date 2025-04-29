import { Link } from 'react-router-dom';
import ReservationCalendar from '../components/features/reservation/ReservationCalendar';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function HomePage() {
    return (
        <div className="container mx-auto py-8">
            {/* Hero Section with Background */}
            <section className="relative rounded-xl overflow-hidden mb-16">
                <div className="bg-primary/10 py-16 px-6 text-center">
                    <h1 className="text-5xl font-bold mb-6">KartingRM</h1>
                    <p className="text-xl mb-8 max-w-2xl mx-auto">
                        ¡Vive la emoción de la velocidad en nuestro kartódromo profesional!
                        La mejor experiencia de carreras en karts de última generación.
                    </p>
                    <Link
                        to="/booking"
                        className="inline-block bg-red-600 text-primary-foreground px-8 py-3 rounded-md
                        shadow-md hover:bg-primary/90 transition-colors text-lg font-medium"
                    >
                        Reservar Ahora
                    </Link>
                </div>
            </section>

            <section className="my-16">
                <h2 className="text-3xl font-bold mb-6 text-center">Calendario de Disponibilidad</h2>
                <Card className="shadow-lg border-2">
                    <CardHeader className="border-b bg-muted/30">
                        <CardTitle className="text-2xl">Reservas Disponibles</CardTitle>
                    </CardHeader>
                    <CardContent className="p-6">
                        <div className="calendar-wrapper">
                            <ReservationCalendar />
                        </div>
                    </CardContent>
                </Card>
            </section>
        </div>
    );
}