using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

namespace ArduSensor
{
    public class BtnColor : MonoBehaviour
    {
        Button targetButton;
        [SerializeField] private Text buttonText;
        [SerializeField] private float lerpDuration = 5.0f;
        [SerializeField] private float colorChangeSpeed = 1.0f;

        private Color initialColor;
        private Color targetColor = Color.red;

        private float t = 0.0f;

        private void Awake()
        {
            if (targetButton == null)
            {
                targetButton = gameObject.GetComponent<Button>();
            }

            if (buttonText == null)
            {
                buttonText = targetButton.GetComponentInChildren<Text>();
            }
        }

        private void Start()
        {
            initialColor = buttonText.color;
            targetButton.onClick.AddListener(ChangeTextColor);
        }

        private void Update()
        {
            if (t < 1.0f)
            {
                t += Time.deltaTime * colorChangeSpeed;
                buttonText.color = Color.Lerp(targetColor, initialColor, t);
            }
        }

        private void ChangeTextColor()
        {
            t = 0.0f;
            buttonText.color = targetColor;
        }
    }
}