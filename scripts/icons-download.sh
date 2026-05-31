#
# Copyright (C) 2026 yuriamw (https://github.com/yuriamw)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#

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