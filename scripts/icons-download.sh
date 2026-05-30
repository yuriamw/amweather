cd ~/AndroidStudioProjects/amweather/app/src/main/res/raw

BASE="https://raw.githubusercontent.com/basmilius/weather-icons/dev/production/fill/svg"

declare -A ICONS=(
  ["clear-day"]="wc_clear_day"
  ["clear-night"]="wc_clear_night"
  ["partly-cloudy-day"]="wc_partly_cloudy_day"
  ["partly-cloudy-night"]="wc_partly_cloudy_night"
  ["cloudy"]="wc_cloudy"
  ["overcast-day"]="wc_overcast_day"
  ["overcast-night"]="wc_overcast_night"
  ["fog"]="wc_fog"
  ["drizzle"]="wc_drizzle"
  ["rain"]="wc_rain"
  ["overcast-rain"]="wc_overcast_rain"
  ["snow"]="wc_snow"
  ["overcast-snow"]="wc_overcast_snow"
  ["sleet"]="wc_sleet"
  ["thunderstorms-rain"]="wc_thunderstorms-rain"
  ["thunderstorms"]="wc_thunderstorms"
  ["wind"]="wc_wind"
)

for NAME in "${!ICONS[@]}"; do
  OUT="${ICONS[$NAME]}.svg"
  echo "Downloading $NAME → $OUT"
  curl -L "$BASE/$NAME.svg" -o "$OUT"
done

echo "Done"