#/bin/sh!

# URL da chunklist mestra
URL=$1

# Caminho de saída do vídeo. Favor não especificar a extensão, sempre será inferida a '.mp4'
OUTPUT=$2

# Caminho para um arquivo temporário da chunklist mestra
PLAYLIST=/tmp/playlist.m3u8

printf "%s\n\n" ":: Baixando chunklist mestra..."
curl "${URL}" -o "${PLAYLIST}"

printf "%s\n\n" ":: Extraindo URL do vídeo de 720p..."
CHUNKLIST_720P=$(cat "${PLAYLIST}" | grep -A 1 1280 | tail -n 1)

if [ -z "$CHUNKLIST_720P" ]; then
	printf "%s\n" "x Falha ao processar a chunklist. Por favor atualize a página de origem do vídeo e recupere a nova URL gerada!"
    exit 1
fi

CHUNKLIST="$(echo "$URL" | awk -F 'playlist.m3u8' '{print $1}')$CHUNKLIST_720P"

# Baixando a chunklist e convertendo para .mp4 utilizando o ffmpeg. Abaixo segue explicações do codec de áudio utilizado
# https://ffmpeg.org/ffmpeg-bitstream-filters.html#aac_005fadtstoasc
# 2.1 aac_adtstoasc
#
# Convert MPEG-2/4 AAC ADTS to an MPEG-4 Audio Specific Configuration bitstream.
# This filter creates an MPEG-4 AudioSpecificConfig from an MPEG-2/4 ADTS header and removes the ADTS header.
# This filter is required for example when copying an AAC stream from a raw ADTS AAC or an MPEG-TS container
# to MP4A-LATM, to an FLV file, or to MOV/MP4 files and related formats such as 3GP or M4A. Please note that
# it is auto-inserted for MP4A-LATM and MOV/MP4 and related formats.
printf "%s\n\n" ":: Baixando vídeo e codificando para '$OUTPUT.mp4'..."
ffmpeg -i "$CHUNKLIST" -c copy -bsf:a aac_adtstoasc "$OUTPUT.mp4"

printf "\n:: Fim :)\n"
